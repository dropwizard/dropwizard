package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.container.ContainerRequestFilter;
import java.security.Principal;

public abstract class AbstractAuthResourceConfig extends DropwizardResourceConfig {

    public AbstractAuthResourceConfig() {
        super(true, new MetricRegistry());

        register(new AuthDynamicFeature(getAuthFilter()));
        register(new AuthValueFactoryProvider.Binder<>(getPrincipalClass()));
        register(RolesAllowedDynamicFeature.class);
    }

    /**
     * @return type of injected principal instance
     */
    protected abstract Class<? extends Principal> getPrincipalClass();

    protected abstract ContainerRequestFilter getAuthFilter();
}
