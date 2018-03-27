package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.text.StrSubstitutor;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TlsSocketAppenderFactoryTest {

    @Rule
    public TcpServer tcpServer = new TcpServer(createServerSocket());

    private ObjectMapper objectMapper = Jackson.newObjectMapper();
    private YamlConfigurationFactory<DefaultLoggingFactory> yamlConfigurationFactory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class, BaseValidator.newValidator(), objectMapper, "dw-ssl");

    @Before
    public void setUp() throws Exception {
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
        SslContextFactory sslContextFactory = new SslContextFactory();
        sslContextFactory.setKeyStorePath(resourcePath("stores/tls_server.jks").getAbsolutePath());
        sslContextFactory.setKeyStorePassword("server_pass");
        sslContextFactory.start();
        return sslContextFactory;
    }

    private static File resourcePath(String path) throws URISyntaxException {
        return new File(Resources.getResource(path).toURI());
    }

    @Test
    public void testTlsLogging() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(new SubstitutingSourceProvider(
            new ResourceConfigurationSourceProvider(), new StrSubstitutor(ImmutableMap.of(
            "tls.trust_store.path", resourcePath("stores/tls_client.jks").getAbsolutePath(),
            "tls.trust_store.pass", "client_pass",
            "tls.server_port", tcpServer.getServerSocket().getLocalPort()
        ))), "yaml/logging-tls.yml");
        loggingFactory.configure(new MetricRegistry(), "tls-appender-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < tcpServer.getMessageCount(); i++) {
            logger.info("Application log {}", i);
        }

        tcpServer.getLatch().await(5, TimeUnit.SECONDS);
        assertThat(tcpServer.getLatch().getCount()).isEqualTo(0);
        loggingFactory.reset();
    }

    @Test
    public void testParseCustomConfiguration() throws Exception {
        DefaultLoggingFactory loggingFactory = yamlConfigurationFactory.build(
            resourcePath("yaml/logging-tls-custom.yml"));
        assertThat(loggingFactory.getAppenders()).hasSize(1);
        TlsSocketAppenderFactory<ILoggingEvent> appenderFactory = (TlsSocketAppenderFactory<ILoggingEvent>)
            loggingFactory.getAppenders().get(0);
        assertThat(appenderFactory.getHost()).isEqualTo("172.16.11.244");
        assertThat(appenderFactory.getPort()).isEqualTo(17002);
        assertThat(appenderFactory.getKeyStorePath()).isEqualTo("/path/to/keystore.p12");
        assertThat(appenderFactory.getKeyStorePassword()).isEqualTo("keystore_pass");
        assertThat(appenderFactory.getKeyStoreType()).isEqualTo("PKCS12");
        assertThat(appenderFactory.getKeyStoreProvider()).isEqualTo("BC");
        assertThat(appenderFactory.getTrustStorePath()).isEqualTo("/path/to/trust_store.jks");
        assertThat(appenderFactory.getTrustStorePassword()).isEqualTo("trust_store_pass");
        assertThat(appenderFactory.getTrustStoreType()).isEqualTo("JKS");
        assertThat(appenderFactory.getTrustStoreProvider()).isEqualTo("SUN");
        assertThat(appenderFactory.getJceProvider()).isEqualTo("Conscrypt");
        assertThat(appenderFactory.isValidateCerts()).isTrue();
        assertThat(appenderFactory.isValidatePeers()).isTrue();
        assertThat(appenderFactory.getSupportedProtocols()).containsExactly("TLSv1.1", "TLSv1.2");
        assertThat(appenderFactory.getExcludedProtocols()).isEmpty();
        assertThat(appenderFactory.getSupportedCipherSuites()).containsExactly("ECDHE-RSA-AES128-GCM-SHA256",
            "ECDHE-ECDSA-AES128-GCM-SHA256");
        assertThat(appenderFactory.getExcludedCipherSuites()).isEmpty();
    }
}
