package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICartRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.IDigitalAssetRepository;
import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.domain.model.Book;
import com.bookstore.bookstore.domain.model.Cart;
import com.bookstore.bookstore.domain.model.Coupon;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponServiceBestCouponTest {

    @Mock
    private ICouponRepository couponRepository;

    @Mock
    private ICartRepository cartRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IDigitalAssetRepository digitalAssetRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void getBestCouponForCart_whenCartIsEmpty_returnsUnavailableResult() {
        UUID userId = UUID.randomUUID();
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.empty());

        var result = couponService.getBestCouponForCart(userId, null, ShippingMethod.DELIVERY);

        assertFalse(result.available());
        assertNull(result.couponCode());
        assertEquals(BigDecimal.ZERO, result.discountAmount());
    }

    @Test
    void getBestCouponForCart_whenMultipleCouponsExist_returnsHighestDiscount() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Cart cart = cart(userId, bookId, 2, 10);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(couponRepository.findAllActive()).thenReturn(List.of(
                coupon(
                        "SAVE10",
                        CouponType.BOOK,
                        CouponDiscountType.PERCENTAGE,
                        new BigDecimal("10"),
                        new BigDecimal("100000"),
                        new BigDecimal("50000"),
                        10,
                        0,
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(5, ChronoUnit.DAYS)
                ),
                coupon(
                        "FIX30",
                        CouponType.BOOK,
                        CouponDiscountType.FIXED_AMOUNT,
                        new BigDecimal("30000"),
                        new BigDecimal("100000"),
                        null,
                        10,
                        0,
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(5, ChronoUnit.DAYS)
                )
        ));
        when(bookRepository.findAllByIdsIncludingDeleted(List.of(bookId)))
                .thenReturn(List.of(book(bookId, new BigDecimal("120000"), 10, null)));
        when(digitalAssetRepository.findAllByIdsActive(List.of())).thenReturn(List.of());

        var result = couponService.getBestCouponForCart(userId, null, ShippingMethod.DELIVERY);

        assertTrue(result.available());
        assertEquals("FIX30", result.couponCode());
        assertEquals(new BigDecimal("30000"), result.discountAmount());
        assertEquals(new BigDecimal("210000"), result.finalAmountEstimate());
    }

    @Test
    void getBestCouponForCart_ignoresExpiredAndMaxedOutCoupons() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Cart cart = cart(userId, bookId, 1, 10);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(couponRepository.findAllActive()).thenReturn(List.of(
                coupon(
                        "EXPIRED",
                        CouponType.BOOK,
                        CouponDiscountType.FIXED_AMOUNT,
                        new BigDecimal("50000"),
                        new BigDecimal("10000"),
                        null,
                        10,
                        0,
                        now.minus(10, ChronoUnit.DAYS),
                        now.minus(1, ChronoUnit.DAYS)
                ),
                coupon(
                        "LIMITED",
                        CouponType.BOOK,
                        CouponDiscountType.FIXED_AMOUNT,
                        new BigDecimal("50000"),
                        new BigDecimal("10000"),
                        null,
                        1,
                        1,
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(1, ChronoUnit.DAYS)
                ),
                coupon(
                        "VALID20",
                        CouponType.BOOK,
                        CouponDiscountType.PERCENTAGE,
                        new BigDecimal("20"),
                        new BigDecimal("10000"),
                        null,
                        10,
                        0,
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(1, ChronoUnit.DAYS)
                )
        ));
        when(bookRepository.findAllByIdsIncludingDeleted(List.of(bookId)))
                .thenReturn(List.of(book(bookId, new BigDecimal("100000"), 10, null)));
        when(digitalAssetRepository.findAllByIdsActive(List.of())).thenReturn(List.of());

        var result = couponService.getBestCouponForCart(userId, null, ShippingMethod.DELIVERY);

        assertTrue(result.available());
        assertEquals("VALID20", result.couponCode());
        assertEquals(new BigDecimal("20000.00"), result.discountAmount());
    }

    @Test
    void getBestCouponForCart_whenRequestedItemDoesNotBelongToCart_throwsCartItemNotFound() {
        UUID userId = UUID.randomUUID();
        Cart cart = cart(userId, UUID.randomUUID(), 1, 10);
        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> couponService.getBestCouponForCart(userId, List.of(UUID.randomUUID()), ShippingMethod.DELIVERY)
        );

        assertEquals(ApplicationErrorCode.CART_ITEM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getBestCouponForCart_whenPhysicalBookIsOutOfStock_returnsUnavailableResult() {
        UUID userId = UUID.randomUUID();
        UUID bookId = UUID.randomUUID();
        Cart cart = cart(userId, bookId, 2, 10);
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);

        when(cartRepository.findByUserId(userId)).thenReturn(Optional.of(cart));
        when(bookRepository.findAllByIdsIncludingDeleted(List.of(bookId)))
                .thenReturn(List.of(book(bookId, new BigDecimal("100000"), 1, null)));
        when(digitalAssetRepository.findAllByIdsActive(List.of())).thenReturn(List.of());

        var result = couponService.getBestCouponForCart(userId, null, ShippingMethod.DELIVERY);

        assertFalse(result.available());
        assertNull(result.couponCode());
        assertEquals(BigDecimal.ZERO, result.discountAmount());
    }

    private static Cart cart(UUID userId, UUID bookId, int quantity, int stockQuantity) {
        Instant now = Instant.EPOCH;
        Cart cart = new Cart(
                UUID.randomUUID(),
                userId,
                List.of(),
                now,
                now
        );
        cart.addPhysicalItem(bookId, quantity, stockQuantity);
        return cart;
    }

    private static Book book(UUID bookId, BigDecimal price, int stockQuantity, Instant deletedAt) {
        Instant now = Instant.EPOCH;
        return new Book(
                bookId,
                "Book title",
                "ISBN-001",
                "Description",
                price,
                stockQuantity,
                List.of(),
                null,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                now,
                now,
                deletedAt
        );
    }

    private static Coupon coupon(
            String code,
            CouponType couponType,
            CouponDiscountType discountType,
            BigDecimal discountValue,
            BigDecimal minOrderAmount,
            BigDecimal maxDiscountAmount,
            Integer maxUsageCount,
            int usedCount,
            Instant startsAt,
            Instant expiresAt
    ) {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return new Coupon(
                UUID.randomUUID(),
                code,
                code,
                couponType,
                discountType,
                discountValue,
                minOrderAmount,
                maxDiscountAmount,
                maxUsageCount,
                usedCount,
                startsAt,
                expiresAt,
                true,
                now.minus(2, ChronoUnit.DAYS),
                now.minus(2, ChronoUnit.DAYS),
                null
        );
    }
}
