package io.dropwizard.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import java.security.Principal;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class CachingWithRecordStatsTest {
    private final Authenticator<String, Principal> authenticator =
            credentials -> Optional.of(new PrincipalImpl(credentials));
    private final Authorizer<Principal> authorizer =
            (principal, role, requestContext) -> "admin".equals(role);

    @Test
    void authenticatorAcceptsASpecThatAlreadyRecordsStats() throws Exception {
        final CachingAuthenticator<String, Principal> cached =
                new CachingAuthenticator<>(
                        new MetricRegistry(), authenticator, CaffeineSpec.parse("maximumSize=10,recordStats"));

        assertThat(cached.authenticate("user").map(Principal::getName)).hasValue("user");
    }

    @Test
    void authorizerAcceptsASpecThatAlreadyRecordsStats() {
        final CachingAuthorizer<Principal> cached =
                new CachingAuthorizer<>(
                        new MetricRegistry(), authorizer, CaffeineSpec.parse("maximumSize=10,recordStats"));

        assertThat(cached.authorize(new PrincipalImpl("user"), "admin", null)).isTrue();
    }
}
