package io.dropwizard.request.logging.logback.access;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LogbackAccessRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private static RequestLogFactory<?> requestLog;

    @BeforeAll
    static void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class);
        requestLog = new YamlConfigurationFactory<>(RequestLogFactory.class,
            BaseValidator.newValidator(), objectMapper, "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/logbackAccessRequestLog.yml");
    }

    @Test
    void testDeserialized() {
        assertThat(requestLog)
            .isInstanceOfSatisfying(LogbackAccessRequestLogFactory.class, logFactory -> assertThat(logFactory)
                .satisfies(logbackAccessRequestLogFactory -> assertThat(logbackAccessRequestLogFactory.getAppenders()).hasSize(1).extractingResultOf("getClass")
                    .containsOnly(ConsoleAppenderFactory.class)));
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackAccessRequestLogFactory.class);
    }
}
