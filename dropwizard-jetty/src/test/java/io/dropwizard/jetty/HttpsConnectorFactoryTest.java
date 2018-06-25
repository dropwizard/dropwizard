package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
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

import static org.apache.commons.lang3.reflect.FieldUtils.getField;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class HttpsConnectorFactoryTest {
    private static final String WINDOWS_MY_KEYSTORE_NAME = "Windows-MY";
    private final Validator validator = BaseValidator.newValidator();

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpsConnectorFactory.class);
    }

    @Test
    public void testParsingConfiguration() throws Exception {
        HttpsConnectorFactory https = new YamlConfigurationFactory<>(HttpsConnectorFactory.class, validator,
                Jackson.newObjectMapper(), "dw-https")
                .build(new File(Resources.getResource("yaml/https-connector.yml").toURI()));

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
    public void testSupportedProtocols() {
        List<String> supportedProtocols = Arrays.asList("SSLv3", "TLS1");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setSupportedProtocols(supportedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory());
        assertThat(Arrays.asList(sslContextFactory.getIncludeProtocols())).isEqualTo(supportedProtocols);
    }

    @Test
    public void testExcludedProtocols() {
        List<String> excludedProtocols = Arrays.asList("SSLv3", "TLS1");

        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
        factory.setExcludedProtocols(excludedProtocols);

        SslContextFactory sslContextFactory = factory.configureSslContextFactory(new SslContextFactory());
        assertThat(Arrays.asList(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);
    }

    @Test
    public void nonWindowsKeyStoreValidation() throws Exception {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        Collection<String> properties = getViolationProperties(validator.validate(factory));
        assertThat(properties.contains("validKeyStorePassword")).isEqualTo(true);
        assertThat(properties.contains("validKeyStorePath")).isEqualTo(true);
    }

    @Test
    public void windowsKeyStoreValidation() throws Exception {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        Collection<String> properties = getViolationProperties(validator.validate(factory));
        assertThat(properties.contains("validKeyStorePassword")).isEqualTo(false);
        assertThat(properties.contains("validKeyStorePath")).isEqualTo(false);
    }

    @Test
    public void canBuildContextFactoryWhenWindowsKeyStoreAvailable() throws Exception {
        // ignore test when Windows Keystore unavailable
        assumeTrue(canAccessWindowsKeyStore());

        final HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);

        assertNotNull(factory.configureSslContextFactory(new SslContextFactory()));
    }

    @Test
    public void windowsKeyStoreUnavailableThrowsException() throws Exception {
        assumeFalse(canAccessWindowsKeyStore());

        final HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        assertThatIllegalStateException().isThrownBy(() ->
            factory.configureSslContextFactory(new SslContextFactory()));
    }

    @Test
    public void testBuild() throws Exception {
        final HttpsConnectorFactory https = new HttpsConnectorFactory();
        https.setBindHost("127.0.0.1");
        https.setPort(8443);

        https.setKeyStorePath("/etc/app/server.ks");
        https.setKeyStoreType("JKS");
        https.setKeyStorePassword("correct_horse");
        https.setKeyStoreProvider("BC");
        https.setTrustStorePath("/etc/app/server.ts");
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
        final Connector connector = https.build(server, metrics, "test-https-connector", threadPool);
        assertThat(connector).isInstanceOf(ServerConnector.class);

        final ServerConnector serverConnector = (ServerConnector) connector;
        assertThat(serverConnector.getPort()).isEqualTo(8443);
        assertThat(serverConnector.getHost()).isEqualTo("127.0.0.1");
        assertThat(serverConnector.getName()).isEqualTo("test-https-connector");
        assertThat(serverConnector.getServer()).isSameAs(server);
        assertThat(serverConnector.getScheduler()).isInstanceOf(ScheduledExecutorScheduler.class);
        assertThat(serverConnector.getExecutor()).isSameAs(threadPool);

        final Jetty93InstrumentedConnectionFactory jetty93SslConnectionFacttory =
            (Jetty93InstrumentedConnectionFactory) serverConnector.getConnectionFactory("ssl");
        assertThat(jetty93SslConnectionFacttory).isInstanceOf(Jetty93InstrumentedConnectionFactory.class);
        assertThat(jetty93SslConnectionFacttory.getTimer()).isSameAs(
            metrics.timer("org.eclipse.jetty.server.HttpConnectionFactory.127.0.0.1.8443.connections"));
        final SslContextFactory sslContextFactory = ((SslConnectionFactory) jetty93SslConnectionFacttory
            .getConnectionFactory()).getSslContextFactory();

        assertThat(getField(SslContextFactory.class, "_keyStoreResource", true).get(sslContextFactory))
            .isEqualTo(Resource.newResource("/etc/app/server.ks"));
        assertThat(sslContextFactory.getKeyStoreType()).isEqualTo("JKS");
        assertThat(getField(SslContextFactory.class, "_keyStorePassword", true).get(sslContextFactory).toString())
            .isEqualTo("correct_horse");
        assertThat(sslContextFactory.getKeyStoreProvider()).isEqualTo("BC");
        assertThat(getField(SslContextFactory.class, "_trustStoreResource", true).get(sslContextFactory))
            .isEqualTo(Resource.newResource("/etc/app/server.ts"));
        assertThat(sslContextFactory.getKeyStoreType()).isEqualTo("JKS");
        assertThat(getField(SslContextFactory.class, "_trustStorePassword", true).get(sslContextFactory).toString())
            .isEqualTo("battery_staple");
        assertThat(sslContextFactory.getKeyStoreProvider()).isEqualTo("BC");
        assertThat(getField(SslContextFactory.class, "_keyManagerPassword", true).get(sslContextFactory).toString())
            .isEqualTo("new_overlords");
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
        assertThat(getField(SslContextFactory.class, "_endpointIdentificationAlgorithm", true).get(sslContextFactory))
            .isEqualTo("HTTPS");
        assertThat(sslContextFactory.isValidateCerts()).isTrue();
        assertThat(sslContextFactory.isValidatePeerCerts()).isTrue();
        assertThat(sslContextFactory.getIncludeProtocols()).containsOnly("TLSv1.1", "TLSv1.2");
        assertThat(sslContextFactory.getIncludeCipherSuites()).containsOnly("TLS_DHE_RSA.*", "TLS_ECDHE.*");

        final ConnectionFactory httpConnectionFactory = serverConnector.getConnectionFactory("http/1.1");
        assertThat(httpConnectionFactory).isInstanceOf(HttpConnectionFactory.class);
        final HttpConfiguration httpConfiguration = ((HttpConnectionFactory) httpConnectionFactory)
            .getHttpConfiguration();
        assertThat(httpConfiguration.getSecureScheme()).isEqualTo("https");
        assertThat(httpConfiguration.getSecurePort()).isEqualTo(8443);
        assertThat(httpConfiguration.getCustomizers()).hasAtLeastOneElementOfType(SecureRequestCustomizer.class);

        connector.stop();
        server.stop();
    }

    @Test
    public void partitionSupportOnlyEnable() {
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
    public void partitionSupportExclude() {
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
    public void partitionSupportInclude() {
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
        if (SystemUtils.IS_OS_WINDOWS) {
            try {
                KeyStore.getInstance(WINDOWS_MY_KEYSTORE_NAME);
                return true;
            } catch (KeyStoreException e) {
                return false;
            }
        }
        return false;
    }

    private <T> Collection<String> getViolationProperties(Set<ConstraintViolation<T>> violations) {
        return violations.stream()
                .map(input -> input.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
