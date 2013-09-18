package com.codahale.dropwizard.server;

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

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests {@link ContextHandlerFactory}
 */
public class ContextHandlerFactoryTest {

    private ContextHandlerFactory handler;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(
                ConsoleAppenderFactory.class,
                FileAppenderFactory.class,
                SyslogAppenderFactory.class);

        this.handler = new ConfigurationFactory<>(
                ContextHandlerFactory.class,
                Validation.buildDefaultValidatorFactory().getValidator(),
                objectMapper, "dw")
                .build(new File(Resources.getResource("yaml/context-handler.yml").toURI()));
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
    public void hasContextPath() throws Exception {
        assertThat(handler.getContextPath()).isNotEmpty();
    }

    @Test
    public void hasValidContextPath() throws Exception {
        assertThat(handler.getContextPath()).startsWith("/");
    }

    @Test
    public void hasExpectedContextPath() throws Exception {
        assertThat(handler.getContextPath()).isEqualTo("/dw-app");
    }
}
