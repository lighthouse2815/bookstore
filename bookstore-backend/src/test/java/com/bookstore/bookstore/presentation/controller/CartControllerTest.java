package com.bookstore.bookstore.presentation.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.ICartService;
import com.bookstore.bookstore.application.port.in.ICouponService;
import com.bookstore.bookstore.application.result.BestCouponSuggestionResult;
import com.bookstore.bookstore.domain.enums.CouponType;
import com.bookstore.bookstore.domain.enums.ShippingMethod;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.presentation.mapper.CartWebMapper;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class CartControllerTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000555");
    private static final UUID CART_ITEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000556");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICartService cartService;

    @MockitoBean
    private ICouponService couponService;

    @MockitoBean
    private CartWebMapper cartWebMapper;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void getBestCoupon_whenAuthenticated_returnsBestCouponSuggestion() throws Exception {
        given(couponService.getBestCouponForCart(USER_ID, List.of(CART_ITEM_ID), ShippingMethod.DELIVERY))
                .willReturn(new BestCouponSuggestionResult(
                        true,
                        "SAVE30",
                        CouponType.BOOK,
                        new BigDecimal("30000"),
                        new BigDecimal("210000"),
                        "Best for cart",
                        null
                ));

        mockMvc.perform(get("/api/cart/best-coupon")
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString())))
                        .param("itemIds", CART_ITEM_ID.toString())
                        .param("shippingMethod", "DELIVERY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.couponCode").value("SAVE30"))
                .andExpect(jsonPath("$.data.discountAmount").value(30000))
                .andExpect(jsonPath("$.data.finalAmountEstimate").value(210000));
    }
}
