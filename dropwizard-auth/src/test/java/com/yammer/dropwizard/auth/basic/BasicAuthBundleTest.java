package com.yammer.dropwizard.auth.basic;

import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BasicAuthBundleTest {
    private final Authenticator<BasicCredentials, String> authenticator = mock(Authenticator.class);
    private final BasicAuthBundle<String> bundle = new BasicAuthBundle<String>(authenticator, "realm");

    @Test
    public void registersABasicAuthProvider() throws Exception {
        final Environment env = mock(Environment.class);

        bundle.initialize(env);

        final ArgumentCaptor<BasicAuthProvider> captor = ArgumentCaptor.forClass(BasicAuthProvider.class);

        verify(env).addProvider(captor.capture());

        assertThat(captor.getValue().getAuthenticator(),
                   is((Object) authenticator));

        assertThat(captor.getValue().getRealm(),
                   is("realm"));
    }
}
