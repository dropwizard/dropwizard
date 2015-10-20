package io.dropwizard.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogFactoryTest {
    private Slf4jRequestLogFactory slf4jRequestLog;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.slf4jRequestLog = new ConfigurationFactory<>(Slf4jRequestLogFactory.class,
                                                     BaseValidator.newValidator(),
                                                     objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(slf4jRequestLog.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("UTC"));
    }
}
