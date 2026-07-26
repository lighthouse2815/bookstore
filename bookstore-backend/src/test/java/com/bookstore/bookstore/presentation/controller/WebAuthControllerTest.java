package com.bookstore.bookstore.presentation.controller;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.infrastructure.security.WebAuthCsrfFilter;
import com.bookstore.bookstore.presentation.mapper.AuthWebMapper;
import com.bookstore.bookstore.presentation.support.ClientRequestMetadataResolver;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WebAuthController.class)
@Import({SecurityConfig.class, AuthWebMapper.class, ClientRequestMetadataResolver.class, WebAuthCsrfFilter.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.auth.web.cookie-secure=true",
        "app.auth.web.cookie-same-site=Lax",
        "app.cors.allowed-origins=https://bookstore.example"
})
class WebAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void webLogin_whenCsrfTokenIsMissing_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/auth/web/login")
                        .header("Origin", "https://bookstore.example")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user@example.com",
                                  "password": "secret-password"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTH_CSRF_INVALID"));

        verifyNoInteractions(authService);
    }

    @Test
    void csrfEndpoint_usesSecureSameSiteCookieInProductionConfiguration() throws Exception {
        mockMvc.perform(get("/api/auth/web/csrf"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Set-Cookie",
                        Matchers.allOf(
                                Matchers.containsString("BOOKSTORE_CSRF="),
                                Matchers.containsString("Secure"),
                                Matchers.containsString("SameSite=Lax"),
                                Matchers.containsString("Path=/")
                        )
                ));
    }
}
