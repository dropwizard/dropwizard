package io.dropwizard.client;

import io.dropwizard.client.ssl.TlsConfiguration;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

public class DropwizardSSLConnectionSocketFactory {

    private final TlsConfiguration configuration;

    public DropwizardSSLConnectionSocketFactory(TlsConfiguration configuration) {
        this.configuration = configuration;
    }

    public SSLConnectionSocketFactory getSocketFactory() throws SSLInitializationException {
        return new SSLConnectionSocketFactory(buildSslContext(), SSLConnectionSocketFactory.getDefaultHostnameVerifier());
    }

    private SSLContext buildSslContext() throws SSLInitializationException {
        final SSLContext sslContext;
        try {
            final SSLContextBuilder sslContextBuilder = new SSLContextBuilder();
            sslContextBuilder.useProtocol(configuration.getProtocol());
            loadKeyMaterial(sslContextBuilder);
            loadTrustMaterial(sslContextBuilder);
            sslContext = sslContextBuilder.build();
        } catch (Exception e) {
            throw new SSLInitializationException(e.getMessage(), e);
        }
        return sslContext;
    }

    private void loadKeyMaterial(SSLContextBuilder sslContextBuilder) throws Exception {
        if(configuration.getKeyStorePath() != null) {
            final KeyStore keystore = loadKeyStore(configuration.getKeyStoreType(), configuration.getKeyStorePath(), configuration.getKeyStorePassword());
            sslContextBuilder.loadKeyMaterial(keystore, configuration.getKeyStorePassword().toCharArray());
        }
    }

    private void loadTrustMaterial(SSLContextBuilder sslContextBuilder) throws Exception {
        KeyStore trustStore = null;
        if(configuration.getTrustStorePath() != null) {
            trustStore = loadKeyStore(configuration.getTrustStoreType(), configuration.getTrustStorePath(), configuration.getTrustStorePassword());
        }
        TrustStrategy trustStrategy = null;
        if(configuration.isTrustSelfSignedCertificates()) {
            trustStrategy = new TrustSelfSignedStrategy();
        }
        sslContextBuilder.loadTrustMaterial(trustStore, trustStrategy);
    }

    private KeyStore loadKeyStore(String type, File path, String password) throws Exception {
        final KeyStore keyStore = KeyStore.getInstance(type);
        InputStream inputStream = new FileInputStream(path);
        keyStore.load(inputStream, password.toCharArray());
        return keyStore;
    }
}
