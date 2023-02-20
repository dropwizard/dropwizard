package io.dropwizard.logging.common;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class TlsSocketAppenderFactoryTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-ssl");

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(TcpSocketAppenderFactory.class);
    }

    private ServerSocket createServerSocket() {
        try {
            return createSslContextFactory().newSslServerSocket("localhost", 0, 0);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private SslContextFactory createSslContextFactory() throws Exception {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath(resourcePath("/stores/tls_server.jks"));
        sslContextFactory.setKeyStorePassword("server_pass");
        sslContextFactory.start();
        return sslContextFactory;
    }

    private String resourcePath(String path) throws URISyntaxException {
        return new File(getClass().getResource(path).toURI()).getAbsolutePath();
    }

    @Test
    void testTlsLogging() throws Exception {
        try (ServerSocket serverSocket = createServerSocket(); TcpServer tcpServer = new TcpServer(serverSocket)) {
            Future<List<String>> receivedMessages = tcpServer.receive();
            DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new SubstitutingSourceProvider(
                new ResourceConfigurationSourceProvider(), new StringSubstitutor(Map.of(
                "tls.trust_store.path", resourcePath("/stores/tls_client.jks"),
                "tls.trust_store.pass", "client_pass",
                "tls.server_port", serverSocket.getLocalPort()
            ))), "/yaml/logging-tls.yml");
            loggingFactory.configure(new MetricRegistry(), "tls-appender-test");

            Logger logger = LoggerFactory.getLogger("com.example.app");
            List<String> loggedMessages = generateLogs(logger);

            loggingFactory.stop();
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
    void testParseCustomConfiguration() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory
            .build(new ResourceConfigurationSourceProvider(), "yaml/logging-tls-custom.yml");
        assertThat(loggingFactory.getAppenders())
            .singleElement()
            .isInstanceOfSatisfying(TlsSocketAppenderFactory.class, appenderFactory -> assertThat(appenderFactory)
                .satisfies(factory -> assertThat(factory.getHost()).isEqualTo("172.16.11.244"))
                .satisfies(factory -> assertThat(factory.getPort()).isEqualTo(17002))
                .satisfies(factory -> assertThat(factory.getKeyStorePath()).isEqualTo("/path/to/keystore.p12"))
                .satisfies(factory -> assertThat(factory.getKeyStorePassword()).isEqualTo("keystore_pass"))
                .satisfies(factory -> assertThat(factory.getKeyStoreType()).isEqualTo("PKCS12"))
                .satisfies(factory -> assertThat(factory.getKeyStoreProvider()).isEqualTo("BC"))
                .satisfies(factory -> assertThat(factory.getTrustStorePath()).isEqualTo("/path/to/trust_store.jks"))
                .satisfies(factory -> assertThat(factory.getTrustStorePassword()).isEqualTo("trust_store_pass"))
                .satisfies(factory -> assertThat(factory.getTrustStoreType()).isEqualTo("JKS"))
                .satisfies(factory -> assertThat(factory.getTrustStoreProvider()).isEqualTo("SUN"))
                .satisfies(factory -> assertThat(factory.getJceProvider()).isEqualTo("Conscrypt"))
                .satisfies(factory -> assertThat(factory.isValidateCerts()).isTrue())
                .satisfies(factory -> assertThat(factory.isValidatePeers()).isTrue())
                .satisfies(factory -> assertThat(factory.getSupportedProtocols()).containsExactly("TLSv1.1", "TLSv1.2"))
                .satisfies(factory -> assertThat(factory.getExcludedProtocols()).isEmpty())
                .satisfies(factory -> assertThat(factory.getSupportedCipherSuites())
                    .containsExactly("ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-ECDSA-AES128-GCM-SHA256"))
                .satisfies(factory -> assertThat(factory.getExcludedCipherSuites()).isEmpty()));
    }

    private static List<String> generateLogs(final Logger logger) {
        return IntStream.range(0, 100)
            .mapToObj(i -> String.format("Application log %d", i))
            .peek(logger::info)
            .collect(Collectors.toList());
    }
}
