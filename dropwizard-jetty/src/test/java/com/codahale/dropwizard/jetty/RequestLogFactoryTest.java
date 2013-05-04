package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.codahale.dropwizard.logging.FileAppenderFactory;
import com.codahale.dropwizard.logging.SyslogAppenderFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

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
