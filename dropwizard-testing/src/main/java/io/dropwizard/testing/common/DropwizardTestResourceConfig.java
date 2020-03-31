package io.dropwizard.testing.common;

import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.validation.HibernateValidationBinder;
import io.dropwizard.setup.ExceptionMapperBinder;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.test.TestProperties;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * A configuration of a Jersey web application by {@link ResourceTestJerseyConfiguration} with
 * support of injecting a configuration from a {@link ServletConfig}. It allows to use it along
 * with the Grizzly web test container.
 */
class DropwizardTestResourceConfig extends DropwizardResourceConfig {

    /**
     * A registry of passed configuration objects. It's used for obtaining the current configuration
     * via a servlet context.
     */
    static final Map<String, ResourceTestJerseyConfiguration> CONFIGURATION_REGISTRY = new ConcurrentHashMap<>();
    static final String CONFIGURATION_ID = "io.dropwizard.testing.junit.resourceTestJerseyConfigurationId";

    DropwizardTestResourceConfig(ResourceTestJerseyConfiguration configuration) {
        super();

        property(TestProperties.CONTAINER_PORT, "0");
        if (configuration.registerDefaultExceptionMappers) {
            register(new ExceptionMapperBinder(false));
        }
        for (Class<?> provider : configuration.providers) {
            register(provider);
        }
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        for (Map.Entry<String, Object> property : configuration.properties.entrySet()) {
            property(property.getKey(), property.getValue());
        }
        register(new JacksonFeature(configuration.mapper));
        register(new HibernateValidationBinder(configuration.validator));
        for (Supplier<?> singleton : configuration.singletons) {
            register(singleton.get());
        }
    }

    DropwizardTestResourceConfig(@Context ServletConfig servletConfig) {
        this(getConfiguration(servletConfig));
    }

    private static ResourceTestJerseyConfiguration getConfiguration(@Context ServletConfig servletConfig) {
        final String id = requireNonNull(servletConfig.getInitParameter(CONFIGURATION_ID), "No configuration id");
        return requireNonNull(CONFIGURATION_REGISTRY.get(id), "No configuration");
    }
}
