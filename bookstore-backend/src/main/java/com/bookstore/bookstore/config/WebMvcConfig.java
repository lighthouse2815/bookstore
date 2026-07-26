package com.bookstore.bookstore.config;

import com.bookstore.bookstore.presentation.support.PaginationRequestInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final PaginationRequestInterceptor paginationRequestInterceptor =
            new PaginationRequestInterceptor();

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(paginationRequestInterceptor)
                .addPathPatterns("/api/**");
    }
}
