package com.yammer.dropwizard.auth.basic.tests;

import com.sun.jersey.core.spi.component.ComponentScope;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.auth.basic.BasicAuthProvider;
import com.yammer.dropwizard.auth.basic.BasicCredentials;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SuppressWarnings("unchecked")
public class BasicAuthProviderTest {
    private final Authenticator<BasicCredentials, String> authenticator = mock(Authenticator.class);
    private final BasicAuthProvider<String> provider = new BasicAuthProvider<String>(authenticator,
                                                                                     "realm");

    @Test
    public void isPerRequest() throws Exception {
        assertThat(provider.getScope())
                .isEqualTo(ComponentScope.PerRequest);
    }
}
