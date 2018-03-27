package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TcpSocketAppenderFactoryTest {
    private static final int TCP_PORT = 24562;

    @Rule
    public TcpServer tcpServer = new TcpServer(createServerSocket());

    private ObjectMapper objectMapper = Jackson.newObjectMapper();
    private YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-tcp");

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(TcpSocketAppenderFactory.class);
    }

    private ServerSocket createServerSocket() {
        try {
            return new ServerSocket(TCP_PORT);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static File resourcePath(String path) throws URISyntaxException {
        return new File(Resources.getResource(path).toURI());
    }

    @Test
    public void testParseConfig() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(resourcePath("yaml/logging-tcp-custom.yml"));
        assertThat(loggingFactory.getAppenders()).hasSize(1);
        TcpSocketAppenderFactory<ILoggingEvent> tcpAppenderFactory = (TcpSocketAppenderFactory<ILoggingEvent>)
            loggingFactory.getAppenders().get(0);
        assertThat(tcpAppenderFactory.getHost()).isEqualTo("172.16.11.245");
        assertThat(tcpAppenderFactory.getPort()).isEqualTo(17001);
        assertThat(tcpAppenderFactory.getConnectionTimeout()).isEqualTo(Duration.milliseconds(100));
        assertThat(tcpAppenderFactory.getSendBufferSize()).isEqualTo(Size.kilobytes(2));
        assertThat(tcpAppenderFactory.isImmediateFlush()).isFalse();
    }

    @Test
    public void testTestTcpLogging() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(resourcePath("yaml/logging-tcp.yml"));
        loggingFactory.configure(new MetricRegistry(), "tcp-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isEqualTo(0);
        loggingFactory.reset();
    }

    @Test
    public void testBufferingTcpLogging() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(resourcePath(
            "yaml/logging-tcp-buffered.yml"));
        loggingFactory.configure(new MetricRegistry(), "tcp-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }
        // We have to flush the buffer manually
        loggingFactory.reset();

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isEqualTo(0);
    }
}
