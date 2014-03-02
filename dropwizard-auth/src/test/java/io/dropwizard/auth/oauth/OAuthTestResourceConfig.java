package io.dropwizard.auth.oauth;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.oauth.OAuthProviderTest.ExampleResource;
import io.dropwizard.jersey.DropwizardResourceConfig;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;

public class OAuthTestResourceConfig extends DropwizardResourceConfig
{
    public OAuthTestResourceConfig()
    {
        super(new MetricRegistry());
        
        final Authenticator<String, String> authenticator = new Authenticator<String, String>() {
            @Override
            public Optional<String> authenticate(String credentials) throws AuthenticationException {
                if ("good-guy".equals(credentials)) {
                    return Optional.of("good-guy");
                }
                if ("bad-guy".equals(credentials)) {
                    throw new AuthenticationException("CRAP", new RuntimeException(""));
                }
                return Optional.absent();
            }
        };
        
        register(AuthFactory.binder(new OAuthFactory<String>(authenticator, "realm", String.class)));
        register(new ExampleResource());
    }
}