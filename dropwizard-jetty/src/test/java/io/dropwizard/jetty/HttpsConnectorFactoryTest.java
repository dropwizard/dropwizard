package io.dropwizard.jetty;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

public class HttpsConnectorFactoryTest {
    private static final String WINDOWS_MY_KEYSTORE_NAME = "Windows-MY";
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpsConnectorFactory.class);
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
        return Collections2.transform(violations, new Function<ConstraintViolation<T>, String>() {
            @Override
            public String apply(ConstraintViolation<T> input) {
                return input.getPropertyPath().toString();
            }
        });
    }
}
