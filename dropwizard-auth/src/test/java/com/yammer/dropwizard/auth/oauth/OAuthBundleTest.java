package com.yammer.dropwizard.auth.oauth;

import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings({"unchecked", "rawtypes"})
public class OAuthBundleTest {
    private final Authenticator<String, String> authenticator = mock(Authenticator.class);
    private final OAuthBundle<String> bundle = new OAuthBundle<String>(authenticator, "realm");

    @Test
    public void registersAnOAuthProvider() throws Exception {
        final Environment env = mock(Environment.class);

        bundle.initialize(env);


        final ArgumentCaptor<OAuthProvider> captor = ArgumentCaptor.forClass(OAuthProvider.class);

        verify(env).addProvider(captor.capture());

        assertThat(captor.getValue().getAuthenticator(),
                   is((Object) authenticator));
        
        assertThat(captor.getValue().getRealm(),
                   is("realm"));
    }
}
