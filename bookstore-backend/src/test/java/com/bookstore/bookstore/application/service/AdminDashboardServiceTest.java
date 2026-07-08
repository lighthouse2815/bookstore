package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.port.out.IBookRepository;
import com.bookstore.bookstore.application.port.out.ICouponRepository;
import com.bookstore.bookstore.application.port.out.IOrderRepository;
import com.bookstore.bookstore.application.port.out.IReviewRepository;
import com.bookstore.bookstore.application.port.out.IUserRepository;
import com.bookstore.bookstore.domain.enums.OrderStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceTest {

    @Mock
    private IOrderRepository orderRepository;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IUserRepository userRepository;

    @Mock
    private IReviewRepository reviewRepository;

    @Mock
    private ICouponRepository couponRepository;

    @InjectMocks
    private AdminDashboardService adminDashboardService;

    @Test
    void getSummary_returnsExpandedDashboardMetrics() {
        when(orderRepository.sumDeliveredRevenueBetween(any(), any())).thenReturn(
                new BigDecimal("900000"),
                new BigDecimal("150000"),
                new BigDecimal("450000")
        );
        when(orderRepository.countCreatedBetween(any(), any())).thenReturn(42L, 5L);
        when(orderRepository.countByStatus(OrderStatus.PENDING)).thenReturn(6L);
        when(orderRepository.countByStatus(OrderStatus.DELIVERED)).thenReturn(28L);
        when(orderRepository.countByStatus(OrderStatus.CANCELLED)).thenReturn(3L);
        when(userRepository.countActiveUsers()).thenReturn(120L);
        when(bookRepository.countActiveBooks()).thenReturn(380L);
        when(bookRepository.countLowStockBooks(10)).thenReturn(8L);
        when(userRepository.countNewCustomersBetween(any(), any())).thenReturn(4L);
        when(reviewRepository.countNewReviewsBetween(any(), any())).thenReturn(9L);
        when(couponRepository.countActiveCouponsAt(any())).thenReturn(11L);

        var result = adminDashboardService.getSummary();

        assertEquals(new BigDecimal("900000"), result.totalRevenue());
        assertEquals(new BigDecimal("150000"), result.todayRevenue());
        assertEquals(new BigDecimal("450000"), result.monthRevenue());
        assertEquals(42L, result.totalOrders());
        assertEquals(5L, result.todayOrders());
        assertEquals(6L, result.pendingOrders());
        assertEquals(28L, result.deliveredOrders());
        assertEquals(3L, result.cancelledOrders());
        assertEquals(120L, result.totalUsers());
        assertEquals(380L, result.totalBooks());
        assertEquals(8L, result.lowStockBooks());
    }
}
