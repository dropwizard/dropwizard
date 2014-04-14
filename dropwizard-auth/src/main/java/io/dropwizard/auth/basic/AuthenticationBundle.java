package io.dropwizard.auth.basic;

import com.sun.jersey.api.container.filter.RolesAllowedResourceFilterFactory;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Bundle;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.UserIdentity;

public class AuthenticationBundle implements Bundle {
    public static final String RESOURCE_FILTERS = "com.sun.jersey.spi.container.ResourceFilters";
    public static final String CONTAINER_REQUEST_FILTERS = "com.sun.jersey.spi.container.ContainerRequestFilters";
    private final boolean requireAuthorization;
    private final String realm;
    private final Authenticator<BasicCredentials, UserIdentity> authenticator;

    public AuthenticationBundle(final Authenticator<BasicCredentials, UserIdentity> authenticator, final boolean requireAuthorization, final String realm) {
        this.authenticator = authenticator;
        this.requireAuthorization = requireAuthorization;
        this.realm = realm;
    }

    @Override
    public void initialize(final Bootstrap<?> bootstrap) {
    }

    @Override
    public void run(final Environment environment) {
        final JerseyEnvironment jersey = environment.jersey();
        final ResourceConfig resourceConfig = jersey.getResourceConfig();
        setupResourceConfig(resourceConfig);
    }

    public void setupResourceConfig(ResourceConfig resourceConfig) {
        resourceConfig.getProperties().put(RESOURCE_FILTERS,
                RolesAllowedResourceFilterFactory.class.getName()
        );

        resourceConfig.getProperties().put(CONTAINER_REQUEST_FILTERS,
                new AuthenticationFilter(authenticator, requireAuthorization, realm));
    }
}
