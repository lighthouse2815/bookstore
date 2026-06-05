package com.bookstore.bookstore.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BcryptPasswordEncoderAdapterTest {

    @Test
    void encodeAndMatches_workTogether() {
        BcryptPasswordEncoderAdapter adapter = new BcryptPasswordEncoderAdapter();

        String encoded = adapter.encode("secret");

        assertTrue(adapter.matches("secret", encoded));
    }
}
