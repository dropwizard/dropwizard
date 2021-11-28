package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Maps;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.text.StringSubstitutor;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class TlsSocketAppenderFactoryTest {

    public TcpServer tcpServer = new TcpServer(createServerSocket());

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-ssl");

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
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new SubstitutingSourceProvider(
            new ResourceConfigurationSourceProvider(), new StringSubstitutor(Maps.of(
            "tls.trust_store.path", resourcePath("/stores/tls_client.jks"),
            "tls.trust_store.pass", "client_pass",
            "tls.server_port", tcpServer.getPort()
        ))), "/yaml/logging-tls.yml");
        loggingFactory.configure(new MetricRegistry(), "tls-appender-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isZero();
        loggingFactory.reset();
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
}
