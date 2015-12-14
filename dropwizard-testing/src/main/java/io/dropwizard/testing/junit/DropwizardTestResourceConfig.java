package io.dropwizard.testing.junit;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.validation.HibernateValidationFeature;
import io.dropwizard.jersey.validation.JerseyViolationExceptionMapper;
import org.glassfish.jersey.server.ServerProperties;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
        super(true, new MetricRegistry());

        if (configuration.registerDefaultExceptionMappers) {
            register(new LoggingExceptionMapper<Throwable>() {
            });
            register(new JerseyViolationExceptionMapper());
            register(new JsonProcessingExceptionMapper());
            register(new EarlyEofExceptionMapper());
        }
        for (Class<?> provider : configuration.providers) {
            register(provider);
        }
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, "true");
        for (Map.Entry<String, Object> property : configuration.properties.entrySet()) {
            property(property.getKey(), property.getValue());
        }
        register(new JacksonMessageBodyProvider(configuration.mapper));
        register(new HibernateValidationFeature(configuration.validator));
        for (Object singleton : configuration.singletons) {
            register(singleton);
        }
    }

    DropwizardTestResourceConfig(@Context ServletConfig servletConfig) {
        this(CONFIGURATION_REGISTRY.get(requireNonNull(servletConfig.getInitParameter(CONFIGURATION_ID))));
    }
}
