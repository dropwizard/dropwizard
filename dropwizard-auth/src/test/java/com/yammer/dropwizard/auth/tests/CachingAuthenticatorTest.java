package com.yammer.dropwizard.auth.tests;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.CachingAuthenticator;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class CachingAuthenticatorTest {
    @SuppressWarnings("unchecked")
    private final Authenticator<String, String> underlying = mock(Authenticator.class);

    private final CachingAuthenticator<String, String> cached =
            CachingAuthenticator.wrap(underlying, CacheBuilderSpec.parse("maximumSize=1"));

    @Before
    public void setUp() throws Exception {
        when(underlying.authenticate(anyString())).thenReturn(Optional.of("principal"));
    }

    @Test
    public void cachesTheFirstReturnedPrincipal() throws Exception {
        assertThat(cached.authenticate("credentials"))
                .isEqualTo(Optional.of("principal"));

        assertThat(cached.authenticate("credentials"))
                .isEqualTo(Optional.of("principal"));

        verify(underlying, times(1)).authenticate("credentials");
    }

    @Test
    public void respectsTheCacheConfiguration() throws Exception {
        cached.authenticate("credentials1");
        cached.authenticate("credentials2");
        cached.authenticate("credentials1");

        final InOrder inOrder = inOrder(underlying);
        inOrder.verify(underlying, times(1)).authenticate("credentials1");
        inOrder.verify(underlying, times(1)).authenticate("credentials2");
        inOrder.verify(underlying, times(1)).authenticate("credentials1");
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
        cached.invalidateAll(ImmutableSet.of("credentials"));
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

        assertThat(cached.size())
                .isEqualTo(1);
    }

    @Test
    public void calculatesCacheStats() throws Exception {
        cached.authenticate("credentials1");

        final CacheStats stats = cached.stats();

        assertThat(stats.loadCount())
                .isEqualTo(1);
    }
}
