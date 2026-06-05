package com.bookstore.bookstore.application.port.out;

public interface IPasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
