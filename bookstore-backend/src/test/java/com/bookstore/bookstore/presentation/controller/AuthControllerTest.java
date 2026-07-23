package com.bookstore.bookstore.presentation.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.port.in.IAuthService;
import com.bookstore.bookstore.application.result.LoginResult;
import com.bookstore.bookstore.domain.enums.UserStatus;
import com.bookstore.bookstore.infrastructure.security.CurrentUserJwtAuthenticationConverter;
import com.bookstore.bookstore.infrastructure.security.SecurityConfig;
import com.bookstore.bookstore.infrastructure.security.WebAuthCsrfFilter;
import com.bookstore.bookstore.presentation.support.ClientRequestMetadataResolver;
import com.bookstore.bookstore.presentation.mapper.AuthWebMapper;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, AuthWebMapper.class, ClientRequestMetadataResolver.class, WebAuthCsrfFilter.class})
@TestPropertySource(properties = {
        "app.jwt.secret=01234567890123456789012345678901",
        "app.jwt.expiration-minutes=60",
        "app.jwt.refresh-expiration-days=30",
        "app.cors.allowed-origins=http://localhost:5173,http://localhost:3000"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAuthService authService;

    @MockitoBean
    private CurrentUserJwtAuthenticationConverter currentUserJwtAuthenticationConverter;

    @Test
    void loginWithGoogle_isAccessibleWithoutAuthentication() throws Exception {
        given(authService.loginWithGoogle(any())).willReturn(new LoginResult(
                UUID.fromString("00000000-0000-0000-0000-000000000001"),
                UserStatus.ACTIVE,
                Set.of("USER"),
                "jwt-token",
                "refresh-token"
        ));

        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": "google-id-token"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void loginWithGoogle_whenIdTokenBlank_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "idToken": " "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("idToken không được để trống"));
    }

    @Test
    void login_whenCredentialsAreInvalid_returnsUnauthorizedWithGenericMessage() throws Exception {
        given(authService.login(any()))
                .willThrow(new ApplicationException(ApplicationErrorCode.AUTH_INVALID_CREDENTIALS));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "missing@example.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Thông tin đăng nhập không hợp lệ"));
    }

    @Test
    void corsPreflight_whenOriginIsNotAllowed_isRejected() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header(HttpHeaders.ORIGIN, "https://evil.example")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden());
    }
}
