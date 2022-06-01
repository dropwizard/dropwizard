package io.dropwizard.testing.common;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.ResourceExtension;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.validation.Validator;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.spi.TestContainerFactory;

/**
 * A configuration of a Jersey testing environment.
 * Encapsulates data required to configure a {@link ResourceExtension}.
 * Primarily accessed via {@link DropwizardTestResourceConfig}.
 */
class ResourceTestJerseyConfiguration {

    final Set<Supplier<?>> singletons;
    final Set<Class<?>> providers;
    final Map<String, Object> properties;
    final ObjectMapper mapper;
    final MetricRegistry metricRegistry;
    final Validator validator;
    final Consumer<ClientConfig> clientConfigurator;
    final TestContainerFactory testContainerFactory;
    final boolean registerDefaultExceptionMappers;

    ResourceTestJerseyConfiguration(
            Set<Supplier<?>> singletons,
            Set<Class<?>> providers,
            Map<String, Object> properties,
            ObjectMapper mapper,
            MetricRegistry metricRegistry,
            Validator validator,
            Consumer<ClientConfig> clientConfigurator,
            TestContainerFactory testContainerFactory,
            boolean registerDefaultExceptionMappers) {
        this.singletons = singletons;
        this.providers = providers;
        this.properties = properties;
        this.mapper = mapper;
        this.metricRegistry = metricRegistry;
        this.validator = validator;
        this.clientConfigurator = clientConfigurator;
        this.testContainerFactory = testContainerFactory;
        this.registerDefaultExceptionMappers = registerDefaultExceptionMappers;
    }

    String getId() {
        return String.valueOf(hashCode());
    }
}
