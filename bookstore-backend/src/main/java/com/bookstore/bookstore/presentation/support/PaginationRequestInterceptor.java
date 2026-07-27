package com.bookstore.bookstore.presentation.support;

import com.bookstore.bookstore.application.exception.ApplicationErrorCode;
import com.bookstore.bookstore.application.exception.ApplicationException;
import com.bookstore.bookstore.application.query.PageQuery;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.method.HandlerMethod;

public class PaginationRequestInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        Integer page = parseInteger(request.getParameter("page"));
        Integer size = parseInteger(request.getParameter("size"));

        if ((page != null && page < 0)
                || (size != null && (size < 1 || size > PageQuery.MAX_SIZE))) {
            throw new ApplicationException(ApplicationErrorCode.PAGINATION_INVALID);
        }
        return true;
    }

    private Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw new ApplicationException(ApplicationErrorCode.PAGINATION_INVALID);
        }
    }
}
