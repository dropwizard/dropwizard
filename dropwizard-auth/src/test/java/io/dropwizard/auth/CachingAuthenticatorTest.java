package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachingAuthenticatorTest {

    private final Authenticator<String, Principal> underlying;
    private final CachingAuthenticator<String, Principal> cached;

    @SuppressWarnings("unchecked")
    public CachingAuthenticatorTest() {
        super();

        final Caffeine<Object, Object> caff = Caffeine.from(CaffeineSpec.parse("maximumSize=1"))
            .executor(MoreExecutors.directExecutor());
        this.underlying = mock(Authenticator.class);
        this.cached = new CachingAuthenticator<>(new MetricRegistry(), this.underlying, caff);
    }

    @BeforeEach
    public void setUp() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.of(new PrincipalImpl("principal")));
    }

    @Test
    public void cachesTheFirstReturnedPrincipal() throws Exception {
        assertThat(cached.authenticate("credentials")).isEqualTo(Optional.<Principal> of(new PrincipalImpl("principal")));
        assertThat(cached.authenticate("credentials")).isEqualTo(Optional.<Principal> of(new PrincipalImpl("principal")));

        verify(underlying, times(1)).authenticate("credentials");
    }

    @Test
    public void invalidatesSingleCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidate("credentials");
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    public void invalidatesSetsOfCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll(Collections.singleton("credentials"));
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    public void invalidatesCredentialsMatchingGivenPredicate() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll("credentials"::equals);
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    public void invalidatesAllCredentials() throws Exception {
        cached.authenticate("credentials");
        cached.invalidateAll();
        cached.authenticate("credentials");

        verify(underlying, times(2)).authenticate("credentials");
    }

    @Test
    public void calculatesTheSizeOfTheCache() throws Exception {
        cached.authenticate("credentials1");
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    public void calculatesCacheStats() throws Exception {
        cached.authenticate("credentials1");
        assertThat(cached.stats().loadCount()).isEqualTo(1);
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    public void shouldNotCacheAbsentPrincipals() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.empty());
        assertThat(cached.authenticate("credentials")).isEqualTo(Optional.empty());
        verify(underlying).authenticate("credentials");
        assertThat(cached.size()).isEqualTo(0);
    }

    @Test
    public void shouldPropagateAuthenticationException() throws AuthenticationException {
        final AuthenticationException e = new AuthenticationException("Auth failed");
        when(underlying.authenticate(anyString())).thenThrow(e);

        assertThatExceptionOfType(AuthenticationException.class)
            .isThrownBy(() -> cached.authenticate("credentials"))
            .satisfies(i -> assertThat(i).isSameAs(e));
    }

    @Test
    public void shouldPropagateRuntimeException() throws AuthenticationException {
        final RuntimeException e = new NullPointerException();
        when(underlying.authenticate(anyString())).thenThrow(e);

        assertThatExceptionOfType(RuntimeException.class)
            .isThrownBy(() -> cached.authenticate("credentials"))
            .satisfies(i -> assertThat(i).isSameAs(e));
    }
}
