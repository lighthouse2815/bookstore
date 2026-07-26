package com.bookstore.bookstore.infrastructure.observability;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.exception.AuthRateLimitException;
import com.bookstore.bookstore.application.result.RefundResult;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class BusinessMetricsAspect {
    private final MeterRegistry meterRegistry;

    @Around("execution(* com.bookstore.bookstore.application.service.OrderService.checkout(..))")
    public Object recordCheckoutFailure(ProceedingJoinPoint joinPoint) throws Throwable {
        try { return joinPoint.proceed(); }
        catch (Throwable throwable) { count("bookstore.checkout.failures"); throw throwable; }
    }

    @Around("execution(* com.bookstore.bookstore.application.service.PaymentExpiryProcessor.expireOne(..))")
    public Object recordPaymentExpiry(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (Boolean.TRUE.equals(result)) count("bookstore.payment.expired");
        return result;
    }

    @Around("execution(* com.bookstore.bookstore.application.service.PaymentReconciliationService.recordIssue(..))")
    public Object recordReconciliationIssue(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed(); count("bookstore.payment.reconciliation.issues"); return result;
    }

    @Around("execution(* com.bookstore.bookstore.application.service.AuthService.login(..)) || execution(* com.bookstore.bookstore.application.service.AuthService.refresh(..))")
    public Object recordAuthFailures(ProceedingJoinPoint joinPoint) throws Throwable {
        try { return joinPoint.proceed(); }
        catch (AuthRateLimitException exception) { count("bookstore.auth.login.throttled"); throw exception; }
        catch (ApplicationException exception) {
            if (exception.getErrorCode() == ApplicationErrorCode.AUTH_REFRESH_REUSE_DETECTED) count("bookstore.auth.refresh.reuse");
            throw exception;
        }
    }

    @Around("execution(* com.bookstore.bookstore.application.service.RefundService.*(..))")
    public Object recordRefundStatus(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if (result instanceof RefundResult refund) Counter.builder("bookstore.refund.transitions")
                .tag("status", refund.status().name().toLowerCase()).register(meterRegistry).increment();
        return result;
    }

    private void count(String name) { Counter.builder(name).register(meterRegistry).increment(); }
}
