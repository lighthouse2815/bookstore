package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateCouponCommand;
import com.bookstore.bookstore.application.command.UpdateCouponCommand;
import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.domain.enums.CouponDiscountType;
import com.bookstore.bookstore.domain.model.Coupon;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void create_savesNormalizedCouponCode() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        when(couponRepository.existsByCodeIncludingDeleted("SALE10")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Coupon result = couponService.create(new CreateCouponCommand(
                " sale10 ",
                " Summer sale ",
                CouponDiscountType.PERCENTAGE,
                new BigDecimal("10"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                100,
                now.minus(1, ChronoUnit.DAYS),
                now.plus(10, ChronoUnit.DAYS),
                true
        ));

        ArgumentCaptor<Coupon> captor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(captor.capture());
        assertEquals("SALE10", captor.getValue().getCode());
        assertEquals("SALE10", result.getCode());
    }

    @Test
    void update_whenChangingToExistingCode_rejectsConflict() {
        Coupon coupon = coupon();
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        when(couponRepository.findByIdActive(coupon.getId())).thenReturn(Optional.of(coupon));
        when(couponRepository.existsByCodeIncludingDeleted("SUMMER20")).thenReturn(true);

        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> couponService.update(new UpdateCouponCommand(
                        coupon.getId(),
                        " summer20 ",
                        coupon.getDescription(),
                        coupon.getDiscountType(),
                        coupon.getDiscountValue(),
                        coupon.getMinOrderAmount(),
                        coupon.getMaxDiscountAmount(),
                        coupon.getMaxUsageCount(),
                        now.minus(1, ChronoUnit.DAYS),
                        now.plus(10, ChronoUnit.DAYS),
                        coupon.isActive()
                ))
        );

        assertEquals(ApplicationErrorCode.COUPON_CODE_ALREADY_EXISTS, exception.getErrorCode());
    }

    private static Coupon coupon() {
        Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        return new Coupon(
                UUID.randomUUID(),
                "SALE10",
                "Sale 10%",
                CouponDiscountType.PERCENTAGE,
                new BigDecimal("10"),
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                100,
                0,
                now.minus(1, ChronoUnit.DAYS),
                now.plus(10, ChronoUnit.DAYS),
                true,
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );
    }
}
