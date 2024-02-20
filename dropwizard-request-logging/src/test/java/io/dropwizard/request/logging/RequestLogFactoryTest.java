package io.dropwizard.request.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLogFactoryTest {
    private LogbackAccessRequestLogFactory logbackAccessRequestLogFactory;

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.logbackAccessRequestLogFactory = new YamlConfigurationFactory<>(LogbackAccessRequestLogFactory.class,
                                                     BaseValidator.newValidator(),
                                                     objectMapper, "dw")
                .build(new ResourceConfigurationSourceProvider(), "yaml/requestLog.yml");
    }

    @Test
    void fileAppenderFactoryIsSet() {
        assertThat(logbackAccessRequestLogFactory)
            .extracting(LogbackAccessRequestLogFactory::getAppenders)
            .asInstanceOf(InstanceOfAssertFactories.LIST)
            .singleElement()
            .isInstanceOf(FileAppenderFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackAccessRequestLogFactory.class);
    }
}
