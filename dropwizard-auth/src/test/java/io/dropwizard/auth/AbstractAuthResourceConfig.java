package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;

import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import java.security.Principal;

public abstract class AbstractAuthResourceConfig extends DropwizardResourceConfig {

    public AbstractAuthResourceConfig() {
        super(true, new MetricRegistry());
        register(getAuthDynamicFeature(getAuthFilter()));
        register(getAuthBinder());
        register(RolesAllowedDynamicFeature.class);
    }

    /**
     * @return The type of injected principal instance.
     */
    protected Class<? extends Principal> getPrincipalClass() {
        return Principal.class;
    }

    /**
     * @return The binder to use for injecting request authentication.
     */
    protected AbstractBinder getAuthBinder() {
        return new AuthValueFactoryProvider.Binder<>(getPrincipalClass());
    }

    /**
     * @return The {@link DynamicFeature} used to register a request
     *         authentication provider.
     */
    protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
        return new AuthDynamicFeature(authFilter);
    }

    /**
     * @return The {@link ContainerRequestFilter} to use for request
     *         authentication.
     */
    protected abstract ContainerRequestFilter getAuthFilter() ;
}
