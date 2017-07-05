package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CachingAuthorizerTest {
    @SuppressWarnings("unchecked")
    private final Authorizer<Principal> underlying = mock(Authorizer.class);
    private final CachingAuthorizer<Principal> cached = new CachingAuthorizer<>(
        new MetricRegistry(),
        underlying,
        CacheBuilderSpec.parse("maximumSize=1")
    );

    private final Principal principal = new PrincipalImpl("principal");
    private final Principal principal2 = new PrincipalImpl("principal2");
    private final Principal principal3 = new PrincipalImpl("principal3");
    private final String role = "popular_kids";

    @Before
    public void setUp() throws Exception {
        when(underlying.authorize(any(), anyString())).thenReturn(true);
    }

    @Test
    public void cachesTheFirstReturnedPrincipal() throws Exception {
        assertThat(cached.authorize(principal, role)).isTrue();
        assertThat(cached.authorize(principal, role)).isTrue();

        verify(underlying, times(1)).authorize(principal, role);
    }

    @Test
    public void respectsTheCacheConfiguration() throws Exception {
        cached.authorize(principal, role);
        cached.authorize(principal2, role);
        cached.authorize(principal, role);

        final InOrder inOrder = inOrder(underlying);
        inOrder.verify(underlying, times(1)).authorize(principal, role);
        inOrder.verify(underlying, times(1)).authorize(principal2, role);
        inOrder.verify(underlying, times(1)).authorize(principal, role);
    }

    @Test
    public void invalidatesPrincipalAndRole() throws Exception {
        cached.authorize(principal, role);
        cached.invalidate(principal, role);
        cached.authorize(principal, role);

        verify(underlying, times(2)).authorize(principal, role);
    }

    @Test
    public void invalidatesSinglePrincipal() throws Exception {
        cached.authorize(principal, role);
        cached.invalidate(principal);
        cached.authorize(principal, role);

        verify(underlying, times(2)).authorize(principal, role);
    }

    @Test
    public void invalidatesSetsofPrincipals() throws Exception {
        cached.authorize(principal, role);
        cached.authorize(principal2, role);
        cached.invalidateAll(ImmutableSet.of(principal, principal2));
        cached.authorize(principal, role);
        cached.authorize(principal2, role);

        verify(underlying, times(2)).authorize(principal, role);
        verify(underlying, times(2)).authorize(principal2, role);
    }

    @Test
    public void invalidatesPrincipalsMatchingGivenPredicate() throws Exception {
        cached.authorize(principal, role);
        cached.invalidateAll(principal::equals);
        cached.authorize(principal, role);

        verify(underlying, times(2)).authorize(principal, role);
    }

    @Test
    public void invalidatesAllPrincipals() throws Exception {
        cached.authorize(principal, role);
        cached.authorize(principal2, role);
        cached.invalidateAll();
        cached.authorize(principal, role);
        cached.authorize(principal2, role);

        verify(underlying, times(2)).authorize(principal, role);
        verify(underlying, times(2)).authorize(principal2, role);
    }

    @Test
    public void calculatesTheSizeOfTheCache() throws Exception {
        assertThat(cached.size()).isEqualTo(0);
        cached.authorize(principal, role);
        assertThat(cached.size()).isEqualTo(1);
        cached.invalidateAll();
        assertThat(cached.size()).isEqualTo(0);
    }

    @Test
    public void calculatesCacheStats() throws Exception {
        assertThat(cached.stats().loadCount()).isEqualTo(0);
        cached.authorize(principal, role);
        assertThat(cached.stats().loadCount()).isEqualTo(1);
        assertThat(cached.size()).isEqualTo(1);
    }

    @Test
    public void shouldPropagateRuntimeException() throws AuthenticationException {
        final RuntimeException e = new NullPointerException();
        when(underlying.authorize(principal, role)).thenThrow(e);
        assertThatNullPointerException()
            .isThrownBy(() -> cached.authorize(principal, role))
            .isSameAs(e);
    }
}
