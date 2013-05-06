package com.codahale.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Iterables;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.codahale.metrics.MetricRegistry.name;

@JsonTypeName("https")
public class HttpsConnectorFactory extends HttpConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsConnectorFactory.class);
    private static final AtomicBoolean LOGGED = new AtomicBoolean(false);

    @NotNull
    private File keyStore;

    @NotNull
    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    private File trustStore;

    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    private String keyManagerPassword;

    private Boolean needClientAuth;
    private Boolean wantClientAuth;
    private String certAlias;
    private File crlPath;
    private Boolean enableCRLDP;
    private Boolean enableOCSP;
    private Integer maxCertPathLength;
    private URI ocspResponderUrl;
    private String jceProvider;
    private boolean validateCerts = true;
    private Boolean validatePeers;
    private List<String> supportedProtocols;
    private List<String> supportedCipherSuites;

    @JsonProperty
    public File getKeyStore() {
        return keyStore;
    }

    @JsonProperty
    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    @JsonProperty
    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    @JsonProperty
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    @JsonProperty
    public String getKeyStoreType() {
        return keyStoreType;
    }

    @JsonProperty
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    @JsonProperty
    public String getTrustStoreType() {
        return trustStoreType;
    }

    @JsonProperty
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @JsonProperty
    public String getKeyManagerPassword() {
        return keyManagerPassword;
    }

    @JsonProperty
    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    @JsonProperty
    public File getTrustStore() {
        return trustStore;
    }

    @JsonProperty
    public void setTrustStore(File trustStore) {
        this.trustStore = trustStore;
    }

    @JsonProperty
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    @JsonProperty
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @JsonProperty
    public Boolean getNeedClientAuth() {
        return needClientAuth;
    }

    @JsonProperty
    public void setNeedClientAuth(Boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    @JsonProperty
    public Boolean getWantClientAuth() {
        return wantClientAuth;
    }

    @JsonProperty
    public void setWantClientAuth(Boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

    @JsonProperty
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @JsonProperty
    public File getCrlPath() {
        return crlPath;
    }

    @JsonProperty
    public void setCrlPath(File crlPath) {
        this.crlPath = crlPath;
    }

    @JsonProperty
    public Boolean getEnableCRLDP() {
        return enableCRLDP;
    }

    @JsonProperty
    public void setEnableCRLDP(Boolean enableCRLDP) {
        this.enableCRLDP = enableCRLDP;
    }

    @JsonProperty
    public Boolean getEnableOCSP() {
        return enableOCSP;
    }

    @JsonProperty
    public void setEnableOCSP(Boolean enableOCSP) {
        this.enableOCSP = enableOCSP;
    }

    @JsonProperty
    public Integer getMaxCertPathLength() {
        return maxCertPathLength;
    }

    @JsonProperty
    public void setMaxCertPathLength(Integer maxCertPathLength) {
        this.maxCertPathLength = maxCertPathLength;
    }

    @JsonProperty
    public URI getOcspResponderUrl() {
        return ocspResponderUrl;
    }

    @JsonProperty
    public void setOcspResponderUrl(URI ocspResponderUrl) {
        this.ocspResponderUrl = ocspResponderUrl;
    }

    @JsonProperty
    public String getJceProvider() {
        return jceProvider;
    }

    @JsonProperty
    public void setJceProvider(String jceProvider) {
        this.jceProvider = jceProvider;
    }

    @JsonProperty
    public Boolean getValidatePeers() {
        return validatePeers;
    }

    @JsonProperty
    public void setValidatePeers(Boolean validatePeers) {
        this.validatePeers = validatePeers;
    }

    @JsonProperty
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @JsonProperty
    public void setSupportedProtocols(List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    @JsonProperty
    public List<String> getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    @JsonProperty
    public void setSupportedCipherSuites(List<String> supportedCipherSuites) {
        this.supportedCipherSuites = supportedCipherSuites;
    }

    @JsonProperty
    public boolean isValidateCerts() {
        return validateCerts;
    }

    @JsonProperty
    public void setValidateCerts(boolean validateCerts) {
        this.validateCerts = validateCerts;
    }

    @Override
    public Connector build(Server server, MetricRegistry metrics, String name, ThreadPool threadPool) {
        logSupportedParameters();

        final HttpConfiguration httpConfig = buildHttpConfiguration();

        final HttpConnectionFactory httpConnectionFactory = buildHttpConnectionFactory(httpConfig);

        final SslContextFactory sslContextFactory = buildSslContextFactory();
        server.addBean(sslContextFactory);

        final SslConnectionFactory sslConnectionFactory =
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.toString());

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = buildBufferPool();

        final String timerName = name(HttpConnectionFactory.class,
                                      getBindHost(),
                                      Integer.toString(getPort()),
                                      "connections");

        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new InstrumentedConnectionFactory(sslConnectionFactory,
                                                                metrics.timer(timerName)),
                              httpConnectionFactory);
    }

    @Override
    protected HttpConfiguration buildHttpConfiguration() {
        final HttpConfiguration config = super.buildHttpConfiguration();
        config.setSecureScheme("https");
        config.setSecurePort(getPort());
        config.addCustomizer(new SecureRequestCustomizer());
        return config;
    }

    protected void logSupportedParameters() {
        if (LOGGED.compareAndSet(false, true)) {
            try {
                final SSLContext context = SSLContext.getDefault();
                final String[] protocols = context.getSupportedSSLParameters().getProtocols();
                final SSLSocketFactory factory = context.getSocketFactory();
                final String[] cipherSuites = factory.getSupportedCipherSuites();
                LOGGER.info("Supported protocols: {}", Arrays.toString(protocols));
                LOGGER.info("Supported cipher suites: {}", Arrays.toString(cipherSuites));
            } catch (NoSuchAlgorithmException ignored) {

            }
        }
    }

    protected SslContextFactory buildSslContextFactory() {
        final SslContextFactory factory = new SslContextFactory(keyStore.getAbsolutePath());
        factory.setKeyStorePassword(keyStorePassword);
        factory.setKeyStoreType(keyStoreType);

        if (trustStore != null) {
            factory.setTrustStorePath(trustStore.getAbsolutePath());
        }
        if (trustStorePassword != null) {
            factory.setTrustStorePassword(trustStorePassword);
        }
        factory.setTrustStoreType(trustStoreType);

        if (keyManagerPassword != null) {
            factory.setKeyManagerPassword(keyManagerPassword);
        }

        if (needClientAuth != null) {
            factory.setNeedClientAuth(needClientAuth);
        }

        if (wantClientAuth != null) {
            factory.setWantClientAuth(wantClientAuth);
        }

        if (certAlias != null) {
            factory.setCertAlias(certAlias);
        }

        if (crlPath != null) {
            factory.setCrlPath(crlPath.getAbsolutePath());
        }

        if (enableCRLDP != null) {
            factory.setEnableCRLDP(enableCRLDP);
        }

        if (enableOCSP != null) {
            factory.setEnableOCSP(enableOCSP);
        }

        if (maxCertPathLength != null) {
            factory.setMaxCertPathLength(maxCertPathLength);
        }

        if (ocspResponderUrl != null) {
            factory.setOcspResponderURL(ocspResponderUrl.toASCIIString());
        }

        if (jceProvider != null) {
            factory.setProvider(jceProvider);
        }

        factory.setValidateCerts(validateCerts);

        if (validatePeers != null) {
            factory.setValidatePeerCerts(validatePeers);
        }

        if (supportedProtocols != null) {
            factory.setIncludeProtocols(Iterables.toArray(supportedProtocols, String.class));
        }

        if (supportedCipherSuites != null) {
            factory.setIncludeCipherSuites(Iterables.toArray(supportedCipherSuites, String.class));
        }

        return factory;
    }
}
