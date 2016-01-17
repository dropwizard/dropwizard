package io.dropwizard.jetty;

import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;
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
        HttpsConnectorFactory https = new ConfigurationFactory<>(HttpsConnectorFactory.class, validator,
                Jackson.newObjectMapper(), "dw-https").
                build(new File(Resources.getResource("yaml/https-connector.yml").toURI()));

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
    	List<String> supportedProtocols = ImmutableList.of("SSLv3", "TLS1");

    	HttpsConnectorFactory factory = new HttpsConnectorFactory();
    	factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
    	factory.setSupportedProtocols(supportedProtocols);

    	SslContextFactory sslContextFactory = factory.buildSslContextFactory();
    	assertThat(ImmutableList.copyOf(sslContextFactory.getIncludeProtocols())).isEqualTo(supportedProtocols);
    }

    @Test
    public void testExcludedProtocols() {
    	List<String> excludedProtocols = ImmutableList.of("SSLv3", "TLS1");

    	HttpsConnectorFactory factory = new HttpsConnectorFactory();
    	factory.setKeyStorePassword("password"); // necessary to avoid a prompt for a password
    	factory.setExcludedProtocols(excludedProtocols);

    	SslContextFactory sslContextFactory = factory.buildSslContextFactory();
    	assertThat(ImmutableList.copyOf(sslContextFactory.getExcludeProtocols())).isEqualTo(excludedProtocols);
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

        assertNotNull(factory.buildSslContextFactory());
    }

    @Test(expected = IllegalStateException.class)
    public void windowsKeyStoreUnavailableThrowsException() throws Exception {
        assumeFalse(canAccessWindowsKeyStore());

        final HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        factory.buildSslContextFactory();
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
        return Collections2.transform(violations, input -> input.getPropertyPath().toString());
    }
}
