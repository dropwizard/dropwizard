package io.dropwizard.testing.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.spi.TestContainerFactory;

import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
    final ValidatorFactory validatorFactory;
    final Validator validator;
    final Consumer<ClientConfig> clientConfigurator;
    final TestContainerFactory testContainerFactory;
    final boolean registerDefaultExceptionMappers;

    ResourceTestJerseyConfiguration(Set<Supplier<?>> singletons, Set<Class<?>> providers, Map<String, Object> properties,
                                    ObjectMapper mapper, ValidatorFactory validatorFactory, Consumer<ClientConfig> clientConfigurator,
                                    TestContainerFactory testContainerFactory, boolean registerDefaultExceptionMappers) {
        this.singletons = singletons;
        this.providers = providers;
        this.properties = properties;
        this.mapper = mapper;
        this.validatorFactory = validatorFactory;
        this.validator = validatorFactory.getValidator();
        this.clientConfigurator = clientConfigurator;
        this.testContainerFactory = testContainerFactory;
        this.registerDefaultExceptionMappers = registerDefaultExceptionMappers;
    }

    String getId() {
        return String.valueOf(hashCode());
    }
}
