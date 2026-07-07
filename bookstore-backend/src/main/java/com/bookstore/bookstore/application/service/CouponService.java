package com.bookstore.bookstore.application.service;

import com.bookstore.bookstore.application.command.CreateCouponCommand;
import com.bookstore.bookstore.application.command.DeleteCouponCommand;
import com.bookstore.bookstore.application.command.UpdateCouponCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.domain.model.Coupon;
import com.bookstore.bookstore.application.result.PageSliceResult;
import com.bookstore.bookstore.shared.util.StringUtils;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService implements ICouponService {

    private final ICouponRepository couponRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Coupon> getAll() {
        return couponRepository.findAllActive();
    }

    @Override
    @Transactional(readOnly = true)
    public PageSliceResult<Coupon> getAll(int page, int size) {
        validatePageRequest(page, size);
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

    private void validatePageRequest(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new ApplicationException(ApplicationErrorCode.INVALID_ARGUMENT, "page");
        }
    }
}
