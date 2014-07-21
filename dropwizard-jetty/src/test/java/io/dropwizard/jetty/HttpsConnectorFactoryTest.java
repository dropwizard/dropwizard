package io.dropwizard.jetty;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Collection;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public class HttpsConnectorFactoryTest {
    private static final String WINDOWS_MY_KEYSTORE_NAME = "Windows-MY";
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(HttpsConnectorFactory.class);
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
    public void windowsKeyStore() throws Exception {
        HttpsConnectorFactory factory = new HttpsConnectorFactory();
        factory.setKeyStoreType(WINDOWS_MY_KEYSTORE_NAME);
        if (canAccessWindowsKeyStore()) {
            factory.buildSslContextFactory();
            return;
        } else {
            try {
                factory.buildSslContextFactory();
                fail("Windows key store should not be supported here");
            } catch (IllegalStateException ex) {
                assertThat(ex.getMessage()).containsIgnoringCase("not supported");
            }
        }
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
