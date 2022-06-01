package io.dropwizard.logging.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TcpSocketAppenderFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory =
            new YamlConfigurationFactory<>(
                    DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-tcp");

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(TcpSocketAppenderFactory.class);
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
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(
                new ResourceConfigurationSourceProvider(), "/yaml/logging-tcp-custom.yml");
        assertThat(loggingFactory.getAppenders())
                .singleElement()
                .isInstanceOfSatisfying(TcpSocketAppenderFactory.class, tcpAppenderFactory -> assertThat(
                                tcpAppenderFactory)
                        .satisfies(factory -> assertThat(factory.getHost()).isEqualTo("172.16.11.245"))
                        .satisfies(factory -> assertThat(factory.getPort()).isEqualTo(17001))
                        .satisfies(factory ->
                                assertThat(factory.getConnectionTimeout()).isEqualTo(Duration.milliseconds(100)))
                        .satisfies(factory ->
                                assertThat(factory.getSendBufferSize()).isEqualTo(DataSize.kibibytes(2)))
                        .satisfies(factory ->
                                assertThat(factory.isImmediateFlush()).isFalse()));
    }

    @Test
    void testTestTcpLogging() throws Exception {
        try (ServerSocket serverSocket = createServerSocket();
                TcpServer tcpServer = new TcpServer(serverSocket)) {
            Future<List<String>> receivedMessages = tcpServer.receive();
            DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(
                    new SubstitutingSourceProvider(
                            new ResourceConfigurationSourceProvider(),
                            new StringSubstitutor(
                                    Collections.singletonMap("tcp.server.port", serverSocket.getLocalPort()))),
                    "yaml/logging-tcp.yml");
            loggingFactory.configure(new MetricRegistry(), "tcp-test");

            List<String> loggedMessages = generateLogs(LoggerFactory.getLogger("com.example.app"));
            loggingFactory.reset();

            assertThat(receivedMessages.get(1, TimeUnit.MINUTES))
                    .hasSize(100)
                    .allSatisfy(s -> assertThat(s).startsWith("INFO"))
                    // Strip preamble from e.g. "INFO  [2021-11-20 10:23:57,620] com.example.app: Application log 0"
                    .extracting(s -> s.substring(s.lastIndexOf("com.example.app: ") + "com.example.app: ".length()))
                    .containsExactlyElementsOf(loggedMessages);
        }
    }

    @Test
    void testBufferingTcpLogging() throws Exception {
        try (ServerSocket serverSocket = createServerSocket();
                TcpServer tcpServer = new TcpServer(serverSocket)) {
            Future<List<String>> receivedMessages = tcpServer.receive();
            DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(
                    new SubstitutingSourceProvider(
                            new ResourceConfigurationSourceProvider(),
                            new StringSubstitutor(
                                    Collections.singletonMap("tcp.server.port", serverSocket.getLocalPort()))),
                    "yaml/logging-tcp-buffered.yml");
            loggingFactory.configure(new MetricRegistry(), "tcp-test");

            List<String> loggedMessages = generateLogs(LoggerFactory.getLogger("com.example.app"));
            // We have to flush the buffer manually
            loggingFactory.reset();

            assertThat(receivedMessages.get(1, TimeUnit.MINUTES))
                    .allSatisfy(s -> assertThat(s).startsWith("INFO"))
                    // Strip preamble from e.g. "INFO  [2021-11-20 10:23:57,620] com.example.app: Application log 0"
                    .extracting(s -> s.substring(s.lastIndexOf("com.example.app: ") + "com.example.app: ".length()))
                    .containsExactlyElementsOf(loggedMessages);
        }
    }

    private static List<String> generateLogs(final Logger logger) {
        return IntStream.range(0, 100)
                .mapToObj(i -> String.format("Application log %d", i))
                .peek(logger::info)
                .collect(Collectors.toList());
    }
}
