package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateCouponCommand;
import com.bookstore.bookstore.application.command.DeleteCouponCommand;
import com.bookstore.bookstore.application.command.UpdateCouponCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.application.query.PageQuery;
import com.bookstore.bookstore.application.result.BestCouponSuggestionResult;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.CartItem;
import com.bookstore.bookstore.domain.model.DigitalAsset;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private static final BigDecimal DELIVERY_SHIPPING_FEE = BigDecimal.valueOf(30_000L);
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = BigDecimal.valueOf(200_000L);
    private static final String DEFAULT_BEST_COUPON_LABEL = "Mã giảm giá tối ưu cho giỏ hàng hiện tại";

    private static final String INELIGIBLE_CART_REASON = "Giỏ hàng có sản phẩm không còn đủ điều kiện để áp dụng coupon";

    private final ICouponRepository couponRepository;
    private final ICartRepository cartRepository;
    private final IBookRepository bookRepository;
    private final IDigitalAssetRepository digitalAssetRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAll() {
        return couponRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Coupon> getAll(PageQuery pageQuery) {
        int page = pageQuery.page();
        int size = pageQuery.size();
        return couponRepository.findPageActive(page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getPublicActivePromotions(Instant at) {
        Instant appliedAt = at == null ? Instant.now() : at;
        return couponRepository.findAllActive().stream()
                .filter(Coupon::isActive)
                .filter(coupon -> !coupon.getStartsAt().isAfter(appliedAt))
                .filter(coupon -> coupon.getExpiresAt().isAfter(appliedAt))
                .filter(coupon -> coupon.getMaxUsageCount() == null || coupon.getUsedCount() < coupon.getMaxUsageCount())
                .sorted(Comparator.comparing(Coupon::getStartsAt))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BestCouponSuggestionResult getBestCouponForCart(
            UUID userId,
            List<UUID> cartItemIds,
            ShippingMethod shippingMethod
    ) {
        if (userId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "userId");
        }

        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            return BestCouponSuggestionResult.unavailable("Giỏ hàng đang trống");
        }

        List<CartItem> selectedItems = selectCartItems(cart.getItems(), cartItemIds);
        if (selectedItems.isEmpty()) {
            return BestCouponSuggestionResult.unavailable("Giỏ hàng đang trống");
        }

        PricingSnapshot pricingSnapshot = calculatePricing(selectedItems, shippingMethod);
        if (!pricingSnapshot.available()) {
            return BestCouponSuggestionResult.unavailable(pricingSnapshot.unavailableReason());
        }
        if (pricingSnapshot.productTotal().compareTo(BigDecimal.ZERO) <= 0) {
            return BestCouponSuggestionResult.unavailable("Giỏ hàng đang trống");
        }

        Instant now = Instant.now();
        BestCouponSuggestionResult bestSuggestion = null;
        for (Coupon coupon : getPublicActivePromotions(now)) {
            BigDecimal discountableAmount = coupon.getCouponType() == CouponType.SHIPPING
                    ? pricingSnapshot.shippingFee()
                    : pricingSnapshot.productTotal();
            if (discountableAmount.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            try {
                BigDecimal discountAmount = coupon.previewDiscount(
                        pricingSnapshot.productTotal(),
                        discountableAmount,
                        now
                );
                if (discountAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                BigDecimal finalAmountEstimate = pricingSnapshot.productTotal()
                        .add(pricingSnapshot.shippingFee())
                        .subtract(discountAmount);

                BestCouponSuggestionResult currentSuggestion = new BestCouponSuggestionResult(
                        true,
                        coupon.getCode(),
                        coupon.getCouponType(),
                        discountAmount,
                        finalAmountEstimate,
                        DEFAULT_BEST_COUPON_LABEL,
                        null
                );

                if (isBetterSuggestion(currentSuggestion, bestSuggestion)) {
                    bestSuggestion = currentSuggestion;
                }
            } catch (DomainException ignored) {
                // Ignore coupons that are not applicable to the current cart snapshot.
            }
        }

        return bestSuggestion != null
                ? bestSuggestion
                : BestCouponSuggestionResult.unavailable("Không có mã giảm giá phù hợp cho giỏ hàng hiện tại");
    }

    @Override
    @Transactional(readOnly = true)
    public Coupon getById(UUID couponId) {
        if (couponId == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "couponId");
        }

        return couponRepository.findByIdActive(couponId)
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon create(CreateCouponCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        String code = normalizeCode(command.code());
        if (couponRepository.existsByCodeIncludingDeleted(code)) {
            throw new ApplicationException(ApplicationErrorCode.COUPON_CODE_ALREADY_EXISTS);
        }

        Instant now = Instant.now();
        Coupon coupon = new Coupon(
                UUID.randomUUID(),
                code,
                StringUtils.trimToNull(command.description()),
                command.couponType(),
                command.discountType(),
                command.discountValue(),
                command.minOrderAmount(),
                command.maxDiscountAmount(),
                command.maxUsageCount(),
                0,
                command.startsAt(),
                command.expiresAt(),
                command.active(),
                now,
                now,
                null
        );

        return couponRepository.save(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Coupon update(UpdateCouponCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Coupon currentCoupon = couponRepository.findByIdActive(command.couponId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));

        String code = normalizeCode(command.code());
        if (!currentCoupon.getCode().equals(code) && couponRepository.existsByCodeIncludingDeleted(code)) {
            throw new ApplicationException(ApplicationErrorCode.COUPON_CODE_ALREADY_EXISTS);
        }

        currentCoupon.updateCoupon(
                code,
                StringUtils.trimToNull(command.description()),
                command.couponType(),
                command.discountType(),
                command.discountValue(),
                command.minOrderAmount(),
                command.maxDiscountAmount(),
                command.maxUsageCount(),
                command.startsAt(),
                command.expiresAt(),
                command.active()
        );

        return couponRepository.save(currentCoupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DeleteCouponCommand command) {
        if (command == null) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "command");
        }

        Coupon currentCoupon = couponRepository.findByIdActive(command.couponId())
                .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.COUPON_NOT_FOUND));

        currentCoupon.softDelete();
        couponRepository.save(currentCoupon);
    }

    private String normalizeCode(String code) {
        String normalizedCode = StringUtils.trimToNull(code);
        return normalizedCode == null ? null : normalizedCode.toUpperCase(Locale.ROOT);
    }

    private List<CartItem> selectCartItems(List<CartItem> cartItems, List<UUID> cartItemIds) {
        if (cartItems == null || cartItems.isEmpty()) {
            return List.of();
        }

        if (cartItemIds == null || cartItemIds.isEmpty()) {
            return List.copyOf(cartItems);
        }

        List<UUID> selectedIds = cartItemIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (selectedIds.isEmpty()) {
            return List.of();
        }

        Map<UUID, CartItem> cartItemsById = new HashMap<>();
        for (CartItem cartItem : cartItems) {
            cartItemsById.put(cartItem.getId(), cartItem);
        }

        List<CartItem> selectedItems = new ArrayList<>(selectedIds.size());
        for (UUID selectedId : selectedIds) {
            CartItem cartItem = cartItemsById.get(selectedId);
            if (cartItem == null) {
                throw new ApplicationException(ApplicationErrorCode.CART_ITEM_NOT_FOUND);
            }
            selectedItems.add(cartItem);
        }
        return selectedItems;
    }

    private PricingSnapshot calculatePricing(List<CartItem> cartItems, ShippingMethod shippingMethod) {
        List<UUID> physicalBookIds = cartItems.stream()
                .filter(CartItem::isPhysicalBook)
                .map(CartItem::getBookId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UUID> digitalAssetIds = cartItems.stream()
                .filter(CartItem::isDigitalAsset)
                .map(CartItem::getDigitalAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<UUID, DigitalAsset> digitalAssetsById = toDigitalAssetMap(digitalAssetRepository.findAllByIdsActive(digitalAssetIds));
        List<UUID> allBookIds = new ArrayList<>(physicalBookIds);
        digitalAssetsById.values().stream()
                .map(DigitalAsset::getBookId)
                .filter(Objects::nonNull)
                .distinct()
                .forEach(allBookIds::add);
        Map<UUID, Book> booksById = toBookMap(bookRepository.findAllByIdsIncludingDeleted(allBookIds));

        String unavailableReason = resolveUnavailableReason(cartItems, booksById, digitalAssetsById);
        if (unavailableReason != null) {
            return PricingSnapshot.unavailable(unavailableReason);
        }

        BigDecimal productTotal = BigDecimal.ZERO;
        boolean hasPhysicalItems = false;
        for (CartItem cartItem : cartItems) {
            if (cartItem.isPhysicalBook()) {
                Book book = booksById.get(cartItem.getBookId());
                if (book == null) {
                    continue;
                }
                hasPhysicalItems = true;
                productTotal = productTotal.add(
                        book.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()))
                );
                continue;
            }

            DigitalAsset digitalAsset = digitalAssetsById.get(cartItem.getDigitalAssetId());
            if (digitalAsset != null) {
                productTotal = productTotal.add(digitalAsset.getPrice());
            }
        }

        ShippingMethod resolvedShippingMethod = resolveShippingMethod(shippingMethod, hasPhysicalItems);
        BigDecimal shippingFee = calculateShippingFee(resolvedShippingMethod, productTotal, hasPhysicalItems);
        return PricingSnapshot.available(productTotal, shippingFee, hasPhysicalItems, resolvedShippingMethod);
    }

    private String resolveUnavailableReason(
            List<CartItem> cartItems,
            Map<UUID, Book> booksById,
            Map<UUID, DigitalAsset> digitalAssetsById
    ) {
        for (CartItem cartItem : cartItems) {
            if (cartItem.isPhysicalBook()) {
                Book book = booksById.get(cartItem.getBookId());
                if (book == null
                        || book.getDeletedAt() != null
                        || book.getStockQuantity() == null
                        || book.getStockQuantity() <= 0
                        || book.getStockQuantity() < cartItem.getQuantity()) {
                    return INELIGIBLE_CART_REASON;
                }
                continue;
            }

            DigitalAsset digitalAsset = digitalAssetsById.get(cartItem.getDigitalAssetId());
            if (digitalAsset == null
                    || !digitalAsset.isPublished()
                    || !digitalAsset.isPurchaseAllowed()) {
                return INELIGIBLE_CART_REASON;
            }

            Book relatedBook = booksById.get(digitalAsset.getBookId());
            if (relatedBook == null || relatedBook.getDeletedAt() != null) {
                return INELIGIBLE_CART_REASON;
            }
        }

        return null;
    }

    private Map<UUID, Book> toBookMap(Collection<Book> books) {
        Map<UUID, Book> booksById = new HashMap<>();
        if (books == null) {
            return booksById;
        }

        for (Book book : books) {
            booksById.put(book.getId(), book);
        }
        return booksById;
    }

    private Map<UUID, DigitalAsset> toDigitalAssetMap(Collection<DigitalAsset> digitalAssets) {
        Map<UUID, DigitalAsset> digitalAssetsById = new HashMap<>();
        if (digitalAssets == null) {
            return digitalAssetsById;
        }

        for (DigitalAsset digitalAsset : digitalAssets) {
            digitalAssetsById.put(digitalAsset.getId(), digitalAsset);
        }
        return digitalAssetsById;
    }

    private ShippingMethod resolveShippingMethod(ShippingMethod shippingMethod, boolean hasPhysicalItems) {
        if (!hasPhysicalItems) {
            return ShippingMethod.PICKUP;
        }

        return shippingMethod == null ? ShippingMethod.DELIVERY : shippingMethod;
    }

    private BigDecimal calculateShippingFee(
            ShippingMethod shippingMethod,
            BigDecimal productTotal,
            boolean hasPhysicalItems
    ) {
        if (!hasPhysicalItems || shippingMethod == ShippingMethod.PICKUP) {
            return BigDecimal.ZERO;
        }

        return productTotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0
                ? BigDecimal.ZERO
                : DELIVERY_SHIPPING_FEE;
    }

    private boolean isBetterSuggestion(
            BestCouponSuggestionResult currentSuggestion,
            BestCouponSuggestionResult bestSuggestion
    ) {
        if (bestSuggestion == null) {
            return true;
        }

        int discountComparison = currentSuggestion.discountAmount()
                .compareTo(bestSuggestion.discountAmount());
        if (discountComparison != 0) {
            return discountComparison > 0;
        }

        int amountComparison = currentSuggestion.finalAmountEstimate()
                .compareTo(bestSuggestion.finalAmountEstimate());
        if (amountComparison != 0) {
            return amountComparison < 0;
        }

        return currentSuggestion.couponCode().compareTo(bestSuggestion.couponCode()) < 0;
    }

    private record PricingSnapshot(
            BigDecimal productTotal,
            BigDecimal shippingFee,
            boolean hasPhysicalItems,
            ShippingMethod shippingMethod,
            String unavailableReason
    ) {
        private static PricingSnapshot available(
                BigDecimal productTotal,
                BigDecimal shippingFee,
                boolean hasPhysicalItems,
                ShippingMethod shippingMethod
        ) {
            return new PricingSnapshot(productTotal, shippingFee, hasPhysicalItems, shippingMethod, null);
        }

        private static PricingSnapshot unavailable(String unavailableReason) {
            return new PricingSnapshot(
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    false,
                    ShippingMethod.PICKUP,
                    unavailableReason
            );
        }

        private boolean available() {
            return unavailableReason == null;
        }
    }
}
