package io.dropwizard.jetty;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.FileAppenderFactory;
import io.dropwizard.logging.SyslogAppenderFactory;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class RequestLogFactoryTest {
    private RequestLogFactory requestLog;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);
        this.requestLog = new ConfigurationFactory<>(RequestLogFactory.class,
                                                     Validation.buildDefaultValidatorFactory()
                                                                       .getValidator(),
                                                     objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(requestLog.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("UTC"));
    }
}
