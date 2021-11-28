package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TcpSocketAppenderFactoryTest {

    private final TcpServer tcpServer = new TcpServer(createServerSocket());
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-tcp");

    @BeforeEach
    void setUp() throws Exception {
        tcpServer.setUp();
        objectMapper.getSubtypeResolver().registerSubtypes(TcpSocketAppenderFactory.class);
    }

    @AfterEach
    void tearDown() throws Exception {
        tcpServer.tearDown();
    }

    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void testParseConfig() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new ResourceConfigurationSourceProvider(), "/yaml/logging-tcp-custom.yml");
        assertThat(loggingFactory.getAppenders())
            .singleElement()
            .isInstanceOfSatisfying(TcpSocketAppenderFactory.class, tcpAppenderFactory -> assertThat(tcpAppenderFactory)
                .satisfies(factory -> assertThat(factory.getHost()).isEqualTo( "172.16.11.245"))
                .satisfies(factory -> assertThat(factory.getPort()).isEqualTo(17001))
                .satisfies(factory -> assertThat(factory.getConnectionTimeout()).isEqualTo(Duration.milliseconds(100)))
                .satisfies(factory -> assertThat(factory.getSendBufferSize()).isEqualTo(DataSize.kibibytes(2)))
                .satisfies(factory -> assertThat(factory.isImmediateFlush()).isFalse()));
    }

    @Test
    void testTestTcpLogging() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new SubstitutingSourceProvider(
                new ResourceConfigurationSourceProvider(),
                new StringSubstitutor(Collections.singletonMap("tcp.server.port", tcpServer.getPort()))),
            "yaml/logging-tcp.yml");
        loggingFactory.configure(new MetricRegistry(), "tcp-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isZero();
        loggingFactory.reset();
    }

    @Test
    void testBufferingTcpLogging() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new SubstitutingSourceProvider(
            new ResourceConfigurationSourceProvider(),
                new StringSubstitutor(Collections.singletonMap("tcp.server.port", tcpServer.getPort()))),
            "yaml/logging-tcp-buffered.yml");
        loggingFactory.configure(new MetricRegistry(), "tcp-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }
        // We have to flush the buffer manually
        loggingFactory.reset();

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isZero();
    }
}
