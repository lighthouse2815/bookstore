package com.bookstore.bookstore.application.port.out;

public interface IAuthThrottleService {

    void assertLoginAllowed(String normalizedIdentifier, String normalizedIp);

    void recordLoginFailure(String normalizedIdentifier, String normalizedIp);

    void clearLoginFailures(String normalizedIdentifier);

    void assertPasswordResetAllowed(String normalizedEmail, String normalizedIp);

    void recordPasswordResetRequest(String normalizedEmail, String normalizedIp);
}
