package com.bookstore.bookstore.infrastructure.security;

import com.bookstore.bookstore.application.port.out.IPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordEncoderAdapter implements IPasswordEncoder {

    private final BCryptPasswordEncoder delegate = new BCryptPasswordEncoder();

    @Override
    public String encode(String rawPassword) {
        return delegate.encode(rawPassword);
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return delegate.matches(rawPassword, encodedPassword);
    }
}
