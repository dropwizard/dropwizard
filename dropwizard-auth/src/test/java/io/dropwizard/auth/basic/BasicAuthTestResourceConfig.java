package io.dropwizard.auth.basic;

import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicAuthProviderTest.ExampleResource;
import io.dropwizard.jersey.DropwizardResourceConfig;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;

public class BasicAuthTestResourceConfig extends DropwizardResourceConfig
{
    public BasicAuthTestResourceConfig()
    {
        super(new MetricRegistry());
        
        final Authenticator<BasicCredentials, String> authenticator = new Authenticator<BasicCredentials, String>() {
            @Override
            public Optional<String> authenticate(BasicCredentials credentials) throws AuthenticationException {
                if ("good-guy".equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.of("good-guy");
                }
                if ("bad-guy".equals(credentials.getUsername())) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };
        
        register(AuthFactory.binder(new BasicAuthFactory<String>(authenticator, "realm", String.class)));
        register(new ExampleResource());
    }
}