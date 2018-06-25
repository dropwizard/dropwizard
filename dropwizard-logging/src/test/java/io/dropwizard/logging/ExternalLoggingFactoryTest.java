package io.dropwizard.logging;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class ExternalLoggingFactoryTest {

    @Test
    public void canBeDeserialized() throws Exception {
        LoggingFactory externalRequestLogFactory = new YamlConfigurationFactory<>(LoggingFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build(new File(Resources.getResource("yaml/logging_external.yml").toURI()));
        assertThat(externalRequestLogFactory).isNotNull();
        assertThat(externalRequestLogFactory).isInstanceOf(ExternalLoggingFactory.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(ExternalLoggingFactory.class);
    }
}
