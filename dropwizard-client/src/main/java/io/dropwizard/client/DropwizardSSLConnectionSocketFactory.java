package io.dropwizard.client;

import io.dropwizard.client.ssl.TlsConfiguration;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.TrustSelfSignedStrategy;
import org.apache.hc.core5.ssl.PrivateKeyStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.ssl.SSLInitializationException;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class DropwizardSSLConnectionSocketFactory {
    private static final Logger log = LoggerFactory.getLogger(DropwizardSSLConnectionSocketFactory.class);

    private final TlsConfiguration configuration;

    @Nullable
    final HostnameVerifier verifier;

    public DropwizardSSLConnectionSocketFactory(TlsConfiguration configuration) {
        this(configuration, null);
    }

    public DropwizardSSLConnectionSocketFactory(TlsConfiguration configuration, @Nullable HostnameVerifier verifier) {
        this.configuration = configuration;
        this.verifier = verifier;
    }

    public SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(buildSslContext(), getSupportedProtocols(), getSupportedCiphers(),
                chooseHostnameVerifier());
    }

    @Nullable
    private String @Nullable [] getSupportedCiphers() {
        final List<String> supportedCiphers = configuration.getSupportedCiphers();
        if (supportedCiphers == null) {
            return null;
        }
        return supportedCiphers.toArray(new String[0]);
    }

    @Nullable
    private String @Nullable [] getSupportedProtocols() {
        final List<String> supportedProtocols = configuration.getSupportedProtocols();
        if (supportedProtocols == null) {
            return null;
        }
        return supportedProtocols.toArray(new String[0]);
    }

    private HostnameVerifier chooseHostnameVerifier() {
        if (configuration.isVerifyHostname()) {
            return verifier != null ? verifier : new DefaultHostnameVerifier();
        } else {
            return new NoopHostnameVerifier();
        }
    }

    private SSLContext buildSslContext() throws SSLInitializationException {
        final SSLContext sslContext;
        try {
            final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.setProtocol(configuration.getProtocol());
            final String configuredProvider = configuration.getProvider();
            if (configuredProvider != null) {
                sslContextBuilder.setProvider(configuredProvider);
            }
            loadKeyMaterial(sslContextBuilder);
            loadTrustMaterial(sslContextBuilder);
            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            throw new SSLInitializationException(e.getMessage(), e);
        }
        return sslContext;
    }

    @Nullable
    private PrivateKeyStrategy choosePrivateKeyStrategy() {
        PrivateKeyStrategy privateKeyStrategy = null;
        if (configuration.getCertAlias() != null) {
            // Unconditionally return our configured alias allowing the consumer
            // to throw an appropriate exception rather than trying to generate our
            // own here if our configured alias is not a key in the aliases map.
            privateKeyStrategy = (aliases, socket) -> configuration.getCertAlias();
        }

        return privateKeyStrategy;
    }

    private void loadKeyMaterial(SSLContextBuilder sslContextBuilder) throws Exception {
        if (configuration.getKeyStorePath() != null) {
            final KeyStore keystore = loadKeyStore(configuration.getKeyStoreType(), configuration.getKeyStorePath(),
                    requireNonNull(configuration.getKeyStorePassword()), configuration.getKeyStoreProvider());

            sslContextBuilder.loadKeyMaterial(keystore,
                    requireNonNull(configuration.getKeyStorePassword()).toCharArray(), choosePrivateKeyStrategy());
        }
    }

    private void loadTrustMaterial(SSLContextBuilder sslContextBuilder) throws Exception {
        KeyStore trustStore = null;
        if (configuration.getTrustStorePath() != null) {
            trustStore = loadKeyStore(configuration.getTrustStoreType(), configuration.getTrustStorePath(),
                    requireNonNull(configuration.getTrustStorePassword()), configuration.getTrustStoreProvider());
        }
        TrustStrategy trustStrategy = null;
        if (configuration.isTrustSelfSignedCertificates()) {
            trustStrategy = new TrustSelfSignedStrategy();
        }
        sslContextBuilder.loadTrustMaterial(trustStore, trustStrategy);
    }

    private static KeyStore loadKeyStore(String type, File path, String password,
                                         @Nullable String provider) throws Exception {
        KeyStore keyStore;

        if (provider == null) {
            keyStore = KeyStore.getInstance(type);

        } else {
            try {
                keyStore = KeyStore.getInstance(type, provider);

            } catch (KeyStoreException ignore) {
                log.warn("Keystore of type: {} is not supported for provider: {}. Trying out other providers...",
                        type, provider);
                keyStore = KeyStore.getInstance(type);
            }
        }

        try (InputStream inputStream = new FileInputStream(path)) {
            keyStore.load(inputStream, password.toCharArray());
        }

        return keyStore;
    }
}
