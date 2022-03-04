package io.dropwizard.logging.common;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalLoggingFactoryTest {

    @Test
    void canBeDeserialized() throws Exception {
        LoggingFactory externalRequestLogFactory = new YamlConfigurationFactory<>(LoggingFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/logging_external.yml");
        assertThat(externalRequestLogFactory)
            .isNotNull()
            .isInstanceOf(ExternalLoggingFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(ExternalLoggingFactory.class);
    }
}
