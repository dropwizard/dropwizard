package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.container.ContainerRequestFilter;
import java.security.Principal;

public abstract class AuthBaseResourceConfig extends DropwizardResourceConfig {
    public AuthBaseResourceConfig() {
        super(true, new MetricRegistry());

        register(new AuthDynamicFeature(getAuthFilter()));
        register(new AuthValueFactoryProvider.Binder<>(Principal.class));
        register(RolesAllowedDynamicFeature.class);
        register(AuthResource.class);
    }

    protected abstract ContainerRequestFilter getAuthFilter();
}
