package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.model.User;

public interface IJwtService {

    String generateAccessToken(User user);
}
