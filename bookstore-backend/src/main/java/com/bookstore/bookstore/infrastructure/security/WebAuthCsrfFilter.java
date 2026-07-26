package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.presentation.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class WebAuthCsrfFilter extends OncePerRequestFilter {

    public static final String CSRF_COOKIE = "BOOKSTORE_CSRF";
    public static final String CSRF_HEADER = "X-CSRF-Token";

    private final CorsProperties corsProperties;
    private final ObjectMapper objectMapper;

    public WebAuthCsrfFilter(CorsProperties corsProperties, ObjectMapper objectMapper) {
        this.corsProperties = corsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/auth/web/")
                || "GET".equalsIgnoreCase(request.getMethod())
                || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String origin = request.getHeader("Origin");
        String cookie = readCookie(request, CSRF_COOKIE);
        String header = request.getHeader(CSRF_HEADER);
        List<String> origins = corsProperties.allowedOrigins();
        boolean trustedOrigin = origin != null && origins != null && origins.contains(origin);
        if (!trustedOrigin || cookie == null || header == null || !constantTimeEquals(cookie, header)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), ApiResponse.error("AUTH_CSRF_INVALID", "Yeu cau bao mat khong hop le"));
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private boolean constantTimeEquals(String left, String right) {
        return java.security.MessageDigest.isEqual(
                left.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                right.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
