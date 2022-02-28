package io.dropwizard.request.logging;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    void canBeDeserialized() throws Exception {
        RequestLogFactory<?> externalRequestLogFactory = new YamlConfigurationFactory<>(RequestLogFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/externalRequestLog.yml");
        assertThat(externalRequestLogFactory)
            .isNotNull()
            .isInstanceOf(ExternalRequestLogFactory.class);
        assertThat(externalRequestLogFactory.isEnabled()).isTrue();
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(ExternalRequestLogFactory.class);
    }
}
