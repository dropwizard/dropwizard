package com.codahale.dropwizard.config;

import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.ConsoleLoggingOutput;
import com.codahale.dropwizard.logging.FileLoggingOutput;
import com.codahale.dropwizard.logging.SyslogLoggingOutput;
import com.codahale.dropwizard.util.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServerConfigurationTest {
    private ServerConfiguration http;

    @Before
    public void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleLoggingOutput.class,
                                                           FileLoggingOutput.class,
                                                           SyslogLoggingOutput.class);

        this.http = new ConfigurationFactory<>(ServerConfiguration.class,
                                               Validation.buildDefaultValidatorFactory()
                                                         .getValidator(),
                                               objectMapper)
                .build(new File(Resources.getResource("yaml/server.yml").toURI()));
    }

    @Test
    public void loadsGzipConfig() throws Exception {
        assertThat(http.getGzipConfiguration().isEnabled())
                .isFalse();
    }

    @Test
    public void hasAServicePort() throws Exception {
        assertThat(http.getPort())
                .isEqualTo(9080);
    }

    @Test
    public void hasAnAdminPort() throws Exception {
        assertThat(http.getAdminPort())
                .isEqualTo(9081);
    }

    @Test
    public void hasAMaximumNumberOfThreads() throws Exception {
        assertThat(http.getMaxThreads())
                .isEqualTo(101);
    }

    @Test
    public void hasAMinimumNumberOfThreads() throws Exception {
        assertThat(http.getMinThreads())
                .isEqualTo(89);
    }

    @Test
    public void hasAnAcceptorThreadCount() throws Exception {
        assertThat(http.getAcceptorThreads())
                .isEqualTo(2);
    }


    @Test
    public void hasAnAcceptQueueSize() throws Exception {
        assertThat(http.getAcceptQueueSize())
                .isEqualTo(100);
    }

    @Test
    public void canReuseAddresses() throws Exception {
        assertThat(http.isReuseAddressEnabled())
                .isFalse();
    }

    @Test
    public void hasAnSoLingerTime() throws Exception {
        assertThat(http.getSoLingerTime())
                .isEqualTo(Optional.of(Duration.seconds(2)));
    }

    @Test
    public void canSendAServerHeader() throws Exception {
        assertThat(http.isServerHeaderEnabled())
                .isTrue();
    }

    @Test
    public void canSendADateHeader() throws Exception {
        assertThat(http.isDateHeaderEnabled())
                .isFalse();
    }

    @Test
    public void canForwardHeaders() throws Exception {
        assertThat(http.useForwardedHeaders())
                .isFalse();
    }

    @Test
    public void canUseDirectBuffers() throws Exception {
        assertThat(http.useDirectBuffers())
                .isFalse();
    }

    @Test
    public void hasABindHost() throws Exception {
        assertThat(http.getBindHost())
                .isEqualTo(Optional.of("localhost"));
    }

    @Test
    public void hasAnAdminUsername() throws Exception {
        assertThat(http.getAdminUsername())
                .isEqualTo(Optional.of("admin"));
    }

    @Test
    public void hasAnAdminPassword() throws Exception {
        assertThat(http.getAdminPassword())
                .isEqualTo(Optional.of("password"));
    }
}
