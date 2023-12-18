package io.dropwizard.request.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import io.dropwizard.request.logging.old.LogbackClassicRequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLogFactoryTest {
    private LogbackClassicRequestLogFactory logbackClassicRequestLogFactory;

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.logbackClassicRequestLogFactory = new YamlConfigurationFactory<>(LogbackClassicRequestLogFactory.class,
                                                     BaseValidator.newValidator(),
                                                     objectMapper, "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/requestLog.yml");
    }

    @Test
    void fileAppenderFactoryIsSet() {
        assertThat(logbackClassicRequestLogFactory)
            .extracting(LogbackClassicRequestLogFactory::getAppenders)
            .asList()
            .singleElement()
            .isInstanceOf(FileAppenderFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackClassicRequestLogFactory.class);
    }
}
