package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class UdpSocketAppenderFactoryTest {

    private static final int UDP_PORT = 32144;

    @Test
    void testSendLogsByTcp() throws Exception {
        final int messageCount = 100;
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(UdpSocketAppenderFactory.class);

        try (DatagramSocket datagramSocket = new DatagramSocket(UDP_PORT); UdpServer udpServer = new UdpServer(datagramSocket, messageCount)) {
            Future<List<String>> receivedMessages = udpServer.receive();
            DefaultLoggingFactory loggingFactory = new YamlConfigurationFactory<>(DefaultLoggingFactory.class,
                BaseValidator.newValidator(), objectMapper, "dw-udp")
                .build(new ResourceConfigurationSourceProvider(), "yaml/logging-udp.yml");
            loggingFactory.configure(new MetricRegistry(), "udp-test");

            Logger logger = LoggerFactory.getLogger("com.example.app");
            List<String> loggedMessages = generateLogs(logger, messageCount)
                .stream()
                .map(msg -> String.format("%s%n", msg))
                .collect(Collectors.toList());
            loggingFactory.reset();

            assertThat(receivedMessages.get(1, TimeUnit.MINUTES))
                .allSatisfy(s -> assertThat(s).startsWith("INFO"))
                // Strip preamble from e.g. "INFO  [2021-11-20 10:23:57,620] com.example.app: Application log 0"
                .extracting(s -> s.substring(s.lastIndexOf("com.example.app: ") + "com.example.app: ".length()))
                .containsExactlyElementsOf(loggedMessages);
        }
    }

    private static List<String> generateLogs(final Logger logger, final int messageCount) {
        return IntStream.range(0, messageCount)
            .mapToObj(i -> String.format("Application log %d", i))
            .peek(logger::info)
            .collect(Collectors.toList());
    }
}
