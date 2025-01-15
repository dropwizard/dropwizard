package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import jakarta.ws.rs.container.ContainerRequestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.security.Principal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingAuthorizerTest {
    @SuppressWarnings("unchecked")
    private final Authorizer<Principal> underlying = mock(Authorizer.class);
    private final CachingAuthorizer<Principal> cached = new CachingAuthorizer<>(
        new MetricRegistry(),
        underlying,
        CaffeineSpec.parse("maximumSize=1")
    );

    private final Principal principal = new PrincipalImpl("principal");
    private final Principal principal2 = new PrincipalImpl("principal2");
    private final String role = "popular_kids";
    private final ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
    private final AuthorizationContext<Principal> authorizationContext = new DefaultAuthorizationContext<>(principal, role, requestContext);
    private final AuthorizationContext<Principal> authorizationContext2 = new DefaultAuthorizationContext<>(principal2, role, requestContext);

    @BeforeEach
    void setUp() {
        when(underlying.authorize(any(), anyString(), any())).thenReturn(true);
        when(underlying.getAuthorizationContext(eq(principal), anyString(), any())).thenReturn(authorizationContext);
        when(underlying.getAuthorizationContext(eq(principal2), anyString(), any())).thenReturn(authorizationContext2);
    }

    @Test
    void cachesTheFirstReturnedPrincipal() {
        assertThat(cached.authorize(principal, role, requestContext)).isTrue();
        assertThat(cached.authorize(principal, role, requestContext)).isTrue();

        verify(underlying, times(1)).authorize(principal, role, requestContext);
    }

    @Test
    void respectsTheCacheConfiguration() {
        cached.authorize(principal, role, requestContext);
        // We need to make sure that background cache invalidation is done before other requests
        cached.cache.cleanUp();
        cached.authorize(principal2, role, requestContext);
        cached.cache.cleanUp();
        cached.authorize(principal, role, requestContext);

        final InOrder inOrder = inOrder(underlying);
        inOrder.verify(underlying, times(1)).authorize(principal, role, requestContext);
        inOrder.verify(underlying, times(1)).authorize(principal2, role, requestContext);
        inOrder.verify(underlying, times(1)).authorize(principal, role, requestContext);
    }

    @Test
    void invalidatesPrincipalAndRole() {
        cached.authorize(principal, role, requestContext);
        cached.invalidate(principal, role, requestContext);
        cached.authorize(principal, role, requestContext);

        verify(underlying, times(2)).authorize(principal, role, requestContext);
    }

    @Test
    void invalidatesSinglePrincipal() {
        cached.authorize(principal, role, requestContext);
        cached.invalidate(principal);
        cached.authorize(principal, role, requestContext);

        verify(underlying, times(2)).authorize(principal, role, requestContext);
    }

    @Test
    void invalidatesSetsofPrincipals() {
        cached.authorize(principal, role, requestContext);
        cached.authorize(principal2, role, requestContext);
        cached.invalidateAll(Set.of(principal, principal2));
        cached.authorize(principal, role, requestContext);
        cached.authorize(principal2, role, requestContext);

        verify(underlying, times(2)).authorize(principal, role, requestContext);
        verify(underlying, times(2)).authorize(principal2, role, requestContext);
    }

    @Test
    void invalidatesPrincipalsMatchingGivenPredicate() {
        cached.authorize(principal, role, requestContext);
        cached.invalidateAll(principal::equals);
        cached.authorize(principal, role, requestContext);

        verify(underlying, times(2)).authorize(principal, role, requestContext);
    }

    @Test
    void invalidatesAllPrincipals() {
        cached.authorize(principal, role, requestContext);
        cached.authorize(principal2, role, requestContext);
        cached.invalidateAll();
        cached.authorize(principal, role, requestContext);
        cached.authorize(principal2, role, requestContext);

        verify(underlying, times(2)).authorize(principal, role, requestContext);
        verify(underlying, times(2)).authorize(principal2, role, requestContext);
    }

    @Test
    void calculatesTheSizeOfTheCache() {
        assertThat(cached.size()).isZero();
        cached.authorize(principal, role, requestContext);
        assertThat(cached.size()).isEqualTo(1);
        cached.invalidateAll();
        assertThat(cached.size()).isZero();
    }

    @Test
    void calculatesCacheStats() {
        assertThat(cached.stats().loadCount()).isZero();
        cached.authorize(principal, role, requestContext);
        assertThat(cached.stats().loadCount()).isEqualTo(1);
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    void shouldPropagateRuntimeException() {
        final RuntimeException e = new NullPointerException();
        when(underlying.authorize(principal, role, requestContext)).thenThrow(e);
        assertThatNullPointerException()
            .isThrownBy(() -> cached.authorize(principal, role, requestContext))
            .isSameAs(e);
    }
}
