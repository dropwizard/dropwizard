package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.metrics.jetty12.InstrumentedConnectionFactory;
import io.dropwizard.validation.BaseValidator;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.security.Credential;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;

import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

class HttpsConnectorFactoryTest {
    private static final String WINDOWS_MY_KEYSTORE_NAME = "Windows-MY";
    private final Validator validator = BaseValidator.newValidator();

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpsConnectorFactory.class);
    }

    @Test
    void testParsingConfiguration() throws Exception {
        HttpsConnectorFactory https = new YamlConfigurationFactory<>(HttpsConnectorFactory.class, validator,
                Jackson.newObjectMapper(), "dw-https")
                .build(new ResourceConfigurationSourceProvider(), "yaml/https-connector.yml");

        assertThat(https.getPort()).isEqualTo(8443);
        assertThat(https.getKeyStorePath()).isEqualTo("/path/to/ks_file");
        assertThat(https.getKeyStorePassword()).isEqualTo("changeit");
        assertThat(https.getKeyStoreType()).isEqualTo("JKS");
        assertThat(https.getTrustStorePath()).isEqualTo("/path/to/ts_file");
        assertThat(https.getTrustStorePassword()).isEqualTo("changeit");
        assertThat(https.getTrustStoreType()).isEqualTo("JKS");
        assertThat(https.getTrustStoreProvider()).isEqualTo("BC");
        assertThat(https.getKeyManagerPassword()).isEqualTo("changeit");
        assertThat(https.getNeedClientAuth()).isTrue();
        assertThat(https.getWantClientAuth()).isTrue();
        assertThat(https.getCertAlias()).isEqualTo("http_server");
        assertThat(https.getCrlPath()).isEqualTo(new File("/path/to/crl_file"));
        assertThat(https.getEnableCRLDP()).isTrue();
        assertThat(https.getEnableOCSP()).isTrue();
        assertThat(https.getMaxCertPathLength()).isEqualTo(3);
        assertThat(https.getOcspResponderUrl()).isEqualTo(new URI("http://ip.example.com:9443/ca/ocsp"));
        assertThat(https.getJceProvider()).isEqualTo("BC");
        assertThat(https.getValidatePeers()).isTrue();
        assertThat(https.getValidatePeers()).isTrue();
        assertThat(https.getSupportedProtocols()).containsExactly("TLSv1.1", "TLSv1.2");
        assertThat(https.getExcludedProtocols()).isEmpty();
        assertThat(https.getSupportedCipherSuites())
                .containsExactly("ECDHE-RSA-AES128-GCM-SHA256", "ECDHE-ECDSA-AES128-GCM-SHA256");
        assertThat(https.getExcludedCipherSuites()).isEmpty();
        assertThat(https.getAllowRenegotiation()).isFalse();
        assertThat(https.getEndpointIdentificationAlgorithm()).isEqualTo("HTTPS");
    }

    @Test
    void testSupportedProtocols() throws Exception {
        List<String> supportedProtocols = Arrays.asList("SSLv3", "TLSv1");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setSupportedProtocols(supportedProtocols);
        factory.setExcludedProtocols(Collections.emptyList());

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getIncludeProtocols())).isEqualTo(supportedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .containsExactlyElementsOf(supportedProtocols);
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    void testSupportedProtocolsWithWildcards() throws Exception {
        List<String> supportedProtocols = Arrays.asList("SSL.*", "TLSv1\\.[01]");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setSupportedProtocols(supportedProtocols);
        factory.setExcludedProtocols(Collections.emptyList());

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getIncludeProtocols())).isEqualTo(supportedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .contains("SSLv3", "TLSv1.1")
                    .doesNotContain("TLSv1.2", "TLSv1.3");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_16)
    void testExcludedProtocols() throws Exception {
        List<String> excludedProtocols = Arrays.asList("SSLv3", "TLSv1");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setExcludedProtocols(excludedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .contains("TLSv1.2")
                    .doesNotContain("SSLv3", "TLSv1");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    @DisabledForJreRange(max = JRE.JAVA_15)
    void testExcludedProtocolsJava16() throws Exception {
        List<String> excludedProtocols = Arrays.asList("SSLv3", "TLSv1");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setExcludedProtocols(excludedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .contains("TLSv1.2", "TLSv1.3")
                    .doesNotContain("SSLv3", "TLSv1");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    @DisabledForJreRange(min = JRE.JAVA_16)
    void testExcludedProtocolsWithWildcards() throws Exception {
        List<String> excludedProtocols = Arrays.asList("SSL.*", "TLSv1(\\.[01])?");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setExcludedProtocols(excludedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .contains("TLSv1.2")
                    .allSatisfy(protocol -> assertThat(protocol).doesNotStartWith("SSL"))
                    .doesNotContain("TLSv1");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    @DisabledForJreRange(max = JRE.JAVA_15)
    void testExcludedProtocolsWithWildcardsJava16() throws Exception {
        List<String> excludedProtocols = Arrays.asList("SSL.*", "TLSv1(\\.[01])?");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setExcludedProtocols(excludedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(Arrays.asList(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .contains("TLSv1.2", "TLSv1.3")
                    .allSatisfy(protocol -> assertThat(protocol).doesNotStartWith("SSL"))
                    .doesNotContain("TLSv1");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    void testDefaultExcludedProtocols() throws Exception {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory.Server());
        assertThat(sslContextFactory.getExcludeProtocols())
                .containsExactlyElementsOf(factory.getExcludedProtocols());

        sslContextFactory.start();
        try {
            assertThat(sslContextFactory.newSSLEngine().getEnabledProtocols())
                    .doesNotContainAnyElementsOf(factory.getExcludedProtocols())
                    .allSatisfy(protocol -> assertThat(protocol).doesNotStartWith("SSL"))
                    .doesNotContain("TLSv1", "TLSv1.1");
        } finally {
            sslContextFactory.stop();
        }
    }

    @Test
    void nonWindowsKeyStoreValidation() {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        assertThat(getViolationProperties(validator.validate(factory)))
                .contains("validKeyStorePassword")
                .contains("validKeyStorePath");
    }

    @Test
    void windowsKeyStoreValidation() {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        assertThat(getViolationProperties(validator.validate(factory)))
                .doesNotContain("validKeyStorePassword")
                .doesNotContain("validKeyStorePath");
    }

    @Test
    void canBuildContextFactoryWhenWindowsKeyStoreAvailable() {
        // ignore test when Windows Keystore unavailable
        assumeTrue(canAccessWindowsKeyStore());

        final HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);

        assertThat(factory.configureSslContextFactory(new SslContextFactory.Server())).isNotNull();
    }

    @Test
    void windowsKeyStoreUnavailableThrowsException() {
        assumeFalse(canAccessWindowsKeyStore());

        final HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        assertThatIllegalStateException().isThrownBy(() ->
                factory.configureSslContextFactory(new SslContextFactory.Server()));
    }

    @Test
    void testBuild() throws Exception {
        final File keyStoreFile = File.createTempFile("server", ".ks");
        keyStoreFile.deleteOnExit();
        final String keyStorePath = keyStoreFile.getPath();
        final File trustStoreFile = File.createTempFile("server", ".ts");
        trustStoreFile.deleteOnExit();
        final String trustStorePath = trustStoreFile.getPath();

        final HttpsConnectorFactory https = new HttpsConnectorFactory();
        https.setBindHost("127.0.0.1");
        https.setPort(8443);

        https.setKeyStorePath(keyStorePath);
        https.setKeyStoreType("JKS");
        https.setKeyStorePassword("correct_horse");
        https.setKeyStoreProvider("BC");
        https.setTrustStorePath(trustStorePath);
        https.setTrustStoreType("JKS");
        https.setTrustStorePassword("battery_staple");
        https.setTrustStoreProvider("BC");

        https.setKeyManagerPassword("new_overlords");
        https.setNeedClientAuth(true);
        https.setWantClientAuth(true);
        https.setCertAlias("alt_server");
        https.setCrlPath(new File("/etc/ctr_list.txt"));
        https.setEnableCRLDP(true);
        https.setEnableOCSP(true);
        https.setMaxCertPathLength(4);
        https.setOcspResponderUrl(new URI("http://windc1/ocsp"));
        https.setJceProvider("BC");
        https.setAllowRenegotiation(false);
        https.setEndpointIdentificationAlgorithm("HTTPS");
        https.setValidateCerts(true);
        https.setValidatePeers(true);
        https.setSupportedProtocols(Arrays.asList("TLSv1.1", "TLSv1.2"));
        https.setSupportedCipherSuites(Arrays.asList("TLS_DHE_RSA.*", "TLS_ECDHE.*"));

        final Server server = new Server();
        final MetricRegistry metrics = new MetricRegistry();
        final ThreadPool threadPool = new QueuedThreadPool();

        try (final ServerConnector serverConnector = (ServerConnector) https.build(server, metrics, "test-https-connector", threadPool)) {
            assertThat(serverConnector.getPort()).isEqualTo(8443);
            assertThat(serverConnector.getHost()).isEqualTo("127.0.0.1");
            assertThat(serverConnector.getName()).isEqualTo("test-https-connector");
            assertThat(serverConnector.getServer()).isSameAs(server);
            assertThat(serverConnector.getScheduler()).isInstanceOf(ScheduledExecutorScheduler.class);
            assertThat(serverConnector.getExecutor()).isSameAs(threadPool);

            final InstrumentedConnectionFactory sslConnectionFactory =
                    (InstrumentedConnectionFactory) serverConnector.getConnectionFactory("ssl");
            assertThat(sslConnectionFactory).isInstanceOf(InstrumentedConnectionFactory.class);
            assertThat(sslConnectionFactory)
                    .extracting("connectionFactory")
                    .asInstanceOf(InstanceOfAssertFactories.type(SslConnectionFactory.class))
                    .extracting(SslConnectionFactory::getSslContextFactory)
                    .satisfies(sslContextFactory -> {
                        assertThat(sslContextFactory.getKeyStoreResource())
                                .isEqualTo(newResource(keyStorePath, server));
                        assertThat(sslContextFactory.getKeyStoreType()).isEqualTo("JKS");
                        assertThat(sslContextFactory).extracting("_keyStoreCredential")
                            .isInstanceOfSatisfying(Credential.class,
                                credential -> assertThat(credential.check("correct_horse")).isTrue());
                        assertThat(sslContextFactory.getKeyStoreProvider()).isEqualTo("BC");
                        assertThat(sslContextFactory.getTrustStoreResource())
                                .isEqualTo(newResource(trustStorePath, server));
                        assertThat(sslContextFactory.getKeyStoreType()).isEqualTo("JKS");
                        assertThat(sslContextFactory).extracting("_trustStoreCredential")
                            .isInstanceOfSatisfying(Credential.class,
                                credential -> assertThat(credential.check("battery_staple")).isTrue());
                        assertThat(sslContextFactory.getKeyStoreProvider()).isEqualTo("BC");
                        assertThat(sslContextFactory).extracting("_keyManagerCredential")
                            .isInstanceOfSatisfying(Credential.class,
                                credential -> assertThat(credential.check("new_overlords")).isTrue());
                        assertThat(sslContextFactory.getNeedClientAuth()).isTrue();
                        assertThat(sslContextFactory.getWantClientAuth()).isTrue();
                        assertThat(sslContextFactory.getCertAlias()).isEqualTo("alt_server");
                        assertThat(sslContextFactory.getCrlPath()).isEqualTo(new File("/etc/ctr_list.txt").getAbsolutePath());
                        assertThat(sslContextFactory.isEnableCRLDP()).isTrue();
                        assertThat(sslContextFactory.isEnableOCSP()).isTrue();
                        assertThat(sslContextFactory.getMaxCertPathLength()).isEqualTo(4);
                        assertThat(sslContextFactory.getOcspResponderURL()).isEqualTo("http://windc1/ocsp");
                        assertThat(sslContextFactory.getProvider()).isEqualTo("BC");
                        assertThat(sslContextFactory.isRenegotiationAllowed()).isFalse();
                        assertThat(sslContextFactory.getEndpointIdentificationAlgorithm()).isEqualTo("HTTPS");
                        assertThat(sslContextFactory.isValidateCerts()).isTrue();
                        assertThat(sslContextFactory.isValidatePeerCerts()).isTrue();
                        assertThat(sslContextFactory.getIncludeProtocols()).containsOnly("TLSv1.1", "TLSv1.2");
                        assertThat(sslContextFactory.getIncludeCipherSuites()).containsOnly("TLS_DHE_RSA.*", "TLS_ECDHE.*");
                    });

            final ConnectionFactory httpConnectionFactory = serverConnector.getConnectionFactory("http/1.1");
            assertThat(httpConnectionFactory).isInstanceOf(HttpConnectionFactory.class);
            final HttpConfiguration httpConfiguration = ((HttpConnectionFactory) httpConnectionFactory)
                    .getHttpConfiguration();
            assertThat(httpConfiguration.getSecureScheme()).isEqualTo("https");
            assertThat(httpConfiguration.getSecurePort()).isEqualTo(8443);
            assertThat(httpConfiguration.getCustomizers()).hasAtLeastOneElementOfType(SecureRequestCustomizer.class);
        } finally {
            server.stop();
        }
    }

    @Test
    void partitionSupportOnlyEnable() {
        final String[] supported = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        final String[] enabled = {"TLSv1", "TLSv1.1", "TLSv1.2"};
        final Map<Boolean, List<String>> partition =
                HttpsConnectorFactory.partitionSupport(supported, enabled, new String[]{}, new String[]{});

        assertThat(partition)
                .containsOnly(
                        entry(true, Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2")),
                        entry(false, Arrays.asList("SSLv2Hello", "SSLv3"))
                );
    }

    @Test
    void partitionSupportExclude() {
        final String[] supported = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        final String[] enabled = {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        final String[] exclude = {"SSL.*"};
        final Map<Boolean, List<String>> partition =
                HttpsConnectorFactory.partitionSupport(supported, enabled, exclude, new String[]{});

        assertThat(partition)
                .containsOnly(
                        entry(true, Arrays.asList("TLSv1", "TLSv1.1", "TLSv1.2")),
                        entry(false, Arrays.asList("SSLv2Hello", "SSLv3"))
                );
    }

    @Test
    void partitionSupportInclude() {
        final String[] supported = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        final String[] enabled = {"SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};
        final String[] exclude = {"SSL*"};
        final String[] include = {"TLSv1.2|SSLv2Hello"};
        final Map<Boolean, List<String>> partition =
                HttpsConnectorFactory.partitionSupport(supported, enabled, exclude, include);

        assertThat(partition)
                .containsOnly(
                        entry(true, Collections.singletonList("TLSv1.2")),
                        entry(false, Arrays.asList("SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1"))
                );
    }

    private boolean canAccessWindowsKeyStore() {
        if (WINDOWS.isCurrentOs()) {
            try {
                KeyStore.getInstance(WINDOWS_MY_KEYSTORE_NAME);
                return true;
            } catch (KeyStoreException e) {
                return false;
            }
        }
        return false;
    }

    private static <T> Collection<String> getViolationProperties(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(input -> input.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private static Resource newResource(String resource, Server server) {
        return ResourceFactory.of(server).newResource(resource);
    }
}
