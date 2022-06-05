package io.dropwizard.request.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RequestLogFactoryTest {
    private LogbackAccessRequestLogFactory logbackAccessRequestLogFactory;

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = new DefaultObjectMapperFactory().newObjectMapper();
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
            .asList()
            .singleElement()
            .isInstanceOf(FileAppenderFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackAccessRequestLogFactory.class);
    }
}
