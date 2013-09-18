package com.codahale.dropwizard.server;

import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.logging.ConsoleAppenderFactory;
import com.codahale.dropwizard.logging.FileAppenderFactory;
import com.codahale.dropwizard.logging.SyslogAppenderFactory;
import com.codahale.dropwizard.server.DefaultHandlerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

/** TODO: Document */
public class DefaultHandlerFactoryTest {

    private DefaultHandlerFactory handler;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(
                ConsoleAppenderFactory.class,
                FileAppenderFactory.class,
                SyslogAppenderFactory.class,
                HttpConnectorFactory.class);

        this.handler = new ConfigurationFactory<>(
                DefaultHandlerFactory.class,
                Validation.buildDefaultValidatorFactory().getValidator(),
                objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/handler.yml").toURI()));
    }

    @Test
    public void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(handler.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    public void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(handler.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    public void hasGzipDisabled() throws Exception {
        assertThat(handler.getGzip().isEnabled()).isFalse();
    }

    @Test
    public void hasAnHttpConnector() throws Exception {
        assertThat(handler.getConnectors()).hasSize(1);
        assertThat(handler.getConnectors().get(0)).isInstanceOf(HttpConnectorFactory.class);
    }
}
