package com.bookstore.bookstore.presentation.controller;

import com.bookstore.bookstore.application.port.in.INewsletterSubscriptionService;
import com.bookstore.bookstore.presentation.mapper.NewsletterWebMapper;
import com.bookstore.bookstore.presentation.request.SubscribeNewsletterRequest;
import com.bookstore.bookstore.presentation.request.UnsubscribeNewsletterRequest;
import com.bookstore.bookstore.presentation.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class NewsletterController {

    private final INewsletterSubscriptionService newsletterSubscriptionService;
    private final NewsletterWebMapper newsletterWebMapper;

    @PostMapping("/api/newsletter/subscriptions")
    public ApiResponse<Void> subscribe(
            HttpServletRequest httpServletRequest,
            @Valid @RequestBody SubscribeNewsletterRequest request
    ) {
        newsletterSubscriptionService.subscribe(
                newsletterWebMapper.toSubscribeCommand(request, httpServletRequest.getRemoteAddr())
        );
        return ApiResponse.success("Đăng ký nhận tin thành công", null);
    }

    @PostMapping("/api/newsletter/subscriptions/unsubscribe")
    public ApiResponse<Void> unsubscribe(@Valid @RequestBody UnsubscribeNewsletterRequest request) {
        newsletterSubscriptionService.unsubscribe(newsletterWebMapper.toUnsubscribeCommand(request));
        return ApiResponse.success("Đã hủy đăng ký nhận tin", null);
    }
}
