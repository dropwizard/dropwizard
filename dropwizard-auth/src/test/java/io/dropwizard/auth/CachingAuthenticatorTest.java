package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CachingAuthenticatorTest {
    private final Caffeine<Object, Object> caff = Caffeine.newBuilder()
            .maximumSize(1L)
            .executor(Runnable::run);

    @Mock(lenient = true)
    private Authenticator<String, Principal> underlying;
    private CachingAuthenticator<String, Principal> cached;

    @BeforeEach
    void setUp() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.of(new PrincipalImpl("principal")));
        cached = new CachingAuthenticator<>(new MetricRegistry(), underlying, caff);
    }

    @Test
    void cachesTheFirstReturnedPrincipal() throws Exception {
        assertThat(cached.authenticate("credentials")).isEqualTo(Optional.<Principal>of(new PrincipalImpl("principal")));
        assertThat(cached.authenticate("credentials")).isEqualTo(Optional.<Principal>of(new PrincipalImpl("principal")));

        verify(underlying, times(1)).authenticate("credentials");
    }

    @Test
    void invalidatesSingleCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidate("credentials");
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    void invalidatesSetsOfCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll(Collections.singleton("credentials"));
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    void invalidatesCredentialsMatchingGivenPredicate() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll("credentials"::equals);
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    void invalidatesAllCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll();
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    void calculatesTheSizeOfTheCache() throws Exception {
        cached.authenticate("credentials1");
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    void calculatesCacheStats() throws Exception {
        cached.authenticate("credentials1");
        assertThat(cached.stats().loadCount()).isEqualTo(1);
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    void shouldNotCacheAbsentPrincipals() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.empty());
        assertThat(cached.authenticate("credentials")).isEmpty();
        verify(underlying).authenticate("credentials");
        assertThat(cached.size()).isZero();
    }

    @Test
    void shouldPropagateAuthenticationException() throws AuthenticationException {
        final AuthenticationException e = new AuthenticationException("Auth failed");
        when(underlying.authenticate(anyString())).thenThrow(e);

        assertThatExceptionOfType(AuthenticationException.class)
                .isThrownBy(() -> cached.authenticate("credentials"))
                .isEqualTo(e);
    }

    @Test
    void shouldPropagateRuntimeException() throws AuthenticationException {
        final RuntimeException e = new NullPointerException();
        when(underlying.authenticate(anyString())).thenThrow(e);

        assertThatRuntimeException()
                .isThrownBy(() -> cached.authenticate("credentials"))
                .isEqualTo(e);
    }

    @Test
    void cachesTheNegativeResultIfSpecified() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.empty());
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder().maximumSize(1L).executor(Runnable::run);
        cached = new CachingAuthenticator<>(new MetricRegistry(), underlying, caffeine, true);
        assertThat(cached.authenticate("credentials")).isEmpty();
        verify(underlying).authenticate("credentials");
        assertThat(cached.size()).isEqualTo(1);
    }
}
