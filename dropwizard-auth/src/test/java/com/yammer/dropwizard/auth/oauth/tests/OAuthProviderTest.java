package com.yammer.dropwizard.auth.oauth.tests;

import com.sun.jersey.core.spi.component.ComponentScope;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.oauth.OAuthProvider;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class OAuthProviderTest {
    private final Authenticator<String, String> authenticator = mock(Authenticator.class);
    private final OAuthProvider<String> provider = new OAuthProvider<String>(authenticator,
                                                                             "realm");

    @Test
    public void isPerRequest() throws Exception {
        assertThat(provider.getScope())
                .isEqualTo(ComponentScope.PerRequest);
    }
}
