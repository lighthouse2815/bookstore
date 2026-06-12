package com.bookstore.bookstore.application.port.out;

import com.bookstore.bookstore.domain.enums.AuthProvider;
import com.bookstore.bookstore.domain.model.UserAuthIdentity;
import java.util.Optional;
import java.util.UUID;

public interface IUserAuthIdentityRepository {

    Optional<UserAuthIdentity> findByProviderAndProviderSubject(AuthProvider provider, String providerSubject);

    Optional<UserAuthIdentity> findByUserIdAndProvider(UUID userId, AuthProvider provider);

    UserAuthIdentity save(UserAuthIdentity userAuthIdentity);
}
