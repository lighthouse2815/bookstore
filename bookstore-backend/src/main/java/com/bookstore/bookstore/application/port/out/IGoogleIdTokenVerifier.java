package com.bookstore.bookstore.application.port.out;

public interface IGoogleIdTokenVerifier {

    VerifiedGoogleIdToken verify(String idToken);
}
