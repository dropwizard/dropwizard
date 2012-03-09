package com.yammer.dropwizard.auth.oauth;

import com.sun.jersey.core.spi.component.ComponentScope;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.Authenticator;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OAuthProviderTest {
    @SuppressWarnings("unchecked")
    private final Authenticator<String, String> authenticator = mock(Authenticator.class);

    private final OAuthProvider<String> provider = new OAuthProvider<String>(authenticator,
                                                                             "realm");

    @Test
    public void isPerRequest() throws Exception {
        assertThat(provider.getScope(),
                   is(ComponentScope.PerRequest));
    }

    @Test
    public void returnsAnOAuthInjectable() throws Exception {
        final Auth auth = mock(Auth.class);
        when(auth.required()).thenReturn(true);

        final OAuthInjectable<String> injectable = (OAuthInjectable<String>) provider.getInjectable(null, auth, null);

        assertThat(injectable.getAuthenticator(),
                   is(authenticator));
        
        assertThat(injectable.getRealm(),
                   is("realm"));
        
        assertThat(injectable.isRequired(),
                   is(true));
    }
}
