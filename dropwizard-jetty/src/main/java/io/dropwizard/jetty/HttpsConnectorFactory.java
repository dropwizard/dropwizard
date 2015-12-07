package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.dropwizard.validation.ValidationMethod;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Builds HTTPS connectors (HTTP over TLS/SSL).
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code keyStorePath}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>
 *             The path to the Java key store which contains the host certificate and private key.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code keyStorePassword}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>
 *             The password used to access the key store.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code keyStoreType}</td>
 *         <td>{@code JKS}</td>
 *         <td>
 *             The type of key store (usually {@code JKS}, {@code PKCS12}, {@code JCEKS},
 *             {@code Windows-MY}, or {@code Windows-ROOT}).
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code keyStoreProvider}</td>
 *         <td>(none)</td>
 *         <td>
 *             The JCE provider to use to access the key store.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code trustStorePath}</td>
 *         <td>(none)</td>
 *         <td>
 *             The path to the Java key store which contains the CA certificates used to establish
 *             trust.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code trustStorePassword}</td>
 *         <td>(none)</td>
 *         <td>The password used to access the trust store.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code trustStoreType}</td>
 *         <td>{@code JKS}</td>
 *         <td>
 *             The type of trust store (usually {@code JKS}, {@code PKCS12}, {@code JCEKS},
 *             {@code Windows-MY}, or {@code Windows-ROOT}).
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code trustStoreProvider}</td>
 *         <td>(none)</td>
 *         <td>
 *             The JCE provider to use to access the trust store.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code keyManagerPassword}</td>
 *         <td>(none)</td>
 *         <td>The password, if any, for the key manager.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code needClientAuth}</td>
 *         <td>(none)</td>
 *         <td>Whether or not client authentication is required.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code wantClientAuth}</td>
 *         <td>(none)</td>
 *         <td>Whether or not client authentication is requested.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code certAlias}</td>
 *         <td>(none)</td>
 *         <td>The alias of the certificate to use.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code crlPath}</td>
 *         <td>(none)</td>
 *         <td>The path to the file which contains the Certificate Revocation List.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code enableCRLDP}</td>
 *         <td>false</td>
 *         <td>Whether or not CRL Distribution Points (CRLDP) support is enabled.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code enableOCSP}</td>
 *         <td>false</td>
 *         <td>Whether or not On-Line Certificate Status Protocol (OCSP) support is enabled.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxCertPathLength}</td>
 *         <td>(unlimited)</td>
 *         <td>The maximum certification path length.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code ocspResponderUrl}</td>
 *         <td>(none)</td>
 *         <td>The location of the OCSP responder.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code jceProvider}</td>
 *         <td>(none)</td>
 *         <td>The name of the JCE provider to use for cryptographic support.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code validateCerts}</td>
 *         <td>true</td>
 *         <td>
 *             Whether or not to validate TLS certificates before starting. If enabled, Dropwizard
 *             will refuse to start with expired or otherwise invalid certificates.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code validatePeers}</td>
 *         <td>true</td>
 *         <td>Whether or not to validate TLS peer certificates.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code supportedProtocols}</td>
 *         <td>(none)</td>
 *         <td>
 *             A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are supported. All
 *             other protocols will be refused.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code excludedProtocols}</td>
 *         <td>(none)</td>
 *         <td>
 *             A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are excluded. These
 *             protocols will be refused.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code supportedCipherSuites}</td>
 *         <td>(none)</td>
 *         <td>
 *             A list of cipher suites (e.g., {@code TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256}) which
 *             are supported. All other cipher suites will be refused
 *         </td>
 *     </tr>
 *    <tr>
 *         <td>{@code excludedCipherSuites}</td>
 *         <td>(none)</td>
 *         <td>
 *             A list of cipher suites (e.g., {@code TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256}) which
 *             are excluded. These cipher suites will be refused.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code allowRenegotiation}</td>
 *         <td>true</td>
 *         <td>Whether or not TLS renegotiation is allowed.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code endpointIdentificationAlgorithm}</td>
 *         <td>(none)</td>
 *         <td>
 *             Which endpoint identification algorithm, if any, to use during the TLS handshake.
 *         </td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link HttpConnectorFactory}.
 *
 * @see HttpConnectorFactory
 */
@JsonTypeName("https")
public class HttpsConnectorFactory extends HttpConnectorFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpsConnectorFactory.class);
    private static final AtomicBoolean LOGGED = new AtomicBoolean(false);

    private String keyStorePath;

    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    private String keyStoreProvider;

    private String trustStorePath;

    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    private String trustStoreProvider;

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
    private boolean validatePeers = true;
    private List<String> supportedProtocols;
    private List<String> excludedProtocols;
    private List<String> supportedCipherSuites;
    private List<String> excludedCipherSuites;
    private boolean allowRenegotiation = true;
    private String endpointIdentificationAlgorithm;

    @JsonProperty
    public boolean getAllowRenegotiation() {
        return allowRenegotiation;
    }

    @JsonProperty
    public void setAllowRenegotiation(boolean allowRenegotiation) {
        this.allowRenegotiation = allowRenegotiation;
    }

    @JsonProperty
    public String getEndpointIdentificationAlgorithm() {
        return endpointIdentificationAlgorithm;
    }

    @JsonProperty
    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm) {
        this.endpointIdentificationAlgorithm = endpointIdentificationAlgorithm;
    }

    @JsonProperty
    public String getKeyStorePath() {
        return keyStorePath;
    }

    @JsonProperty
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
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
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    @JsonProperty
    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
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
    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    @JsonProperty
    public void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
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
    public String getTrustStorePath() {
        return trustStorePath;
    }

    @JsonProperty
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
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
    public boolean getValidatePeers() {
        return validatePeers;
    }

    @JsonProperty
    public void setValidatePeers(boolean validatePeers) {
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
    public List<String> getExcludedProtocols() {
        return excludedProtocols;
    }

    @JsonProperty
    public void setExcludedProtocols(List<String> excludedProtocols) {
        this.excludedProtocols = excludedProtocols;
    }

    @JsonProperty
    public List<String> getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    @JsonProperty
    public List<String> getExcludedCipherSuites() {
        return excludedCipherSuites;
    }

    @JsonProperty
    public void setExcludedCipherSuites(List<String> excludedCipherSuites) {
        this.excludedCipherSuites = excludedCipherSuites;
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

    @ValidationMethod(message = "keyStorePath should not be null")
    public boolean isValidKeyStorePath() {
        return keyStoreType.startsWith("Windows-") || keyStorePath != null;
    }

    @ValidationMethod(message = "keyStorePassword should not be null or empty")
    public boolean isValidKeyStorePassword() {
        return keyStoreType.startsWith("Windows-") ||
                !Strings.isNullOrEmpty(keyStorePassword);
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

        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new Jetty93InstrumentedConnectionFactory(
                                      sslConnectionFactory,
                                      metrics.timer(httpConnections())),
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

                if (getSupportedProtocols() != null) {
                    LOGGER.info("Configured protocols: {}", getSupportedProtocols());
                }

                if (getExcludedProtocols() != null) {
                    LOGGER.info("Excluded protocols: {}", getExcludedProtocols());
                }

                if (getSupportedCipherSuites() != null) {
                    LOGGER.info("Configured cipher suites: {}", getSupportedCipherSuites());
                }

                if (getExcludedCipherSuites() != null) {
                    LOGGER.info("Excluded cipher suites: {}", getExcludedCipherSuites());
                }
            } catch (NoSuchAlgorithmException ignored) {

            }
        }
    }

    protected SslContextFactory buildSslContextFactory() {
        final SslContextFactory factory = new SslContextFactory();
        if (keyStorePath != null) {
            factory.setKeyStorePath(keyStorePath);
        }

        final String keyStoreType = getKeyStoreType();
        if (keyStoreType.startsWith("Windows-")) {
            try {
                final KeyStore keyStore = KeyStore.getInstance(keyStoreType);

                keyStore.load(null, null);
                factory.setKeyStore(keyStore);
            } catch (Exception e) {
                throw new IllegalStateException("Windows key store not supported", e);
            }
        } else {
            factory.setKeyStoreType(keyStoreType);
            factory.setKeyStorePassword(keyStorePassword);
        }

        if (keyStoreProvider != null) {
            factory.setKeyStoreProvider(keyStoreProvider);
        }

        final String trustStoreType = getTrustStoreType();
        if (trustStoreType.startsWith("Windows-")) {
          try {
            final KeyStore keyStore = KeyStore.getInstance(trustStoreType);

            keyStore.load(null, null);
            factory.setTrustStore(keyStore);
          } catch (Exception e) {
            throw new IllegalStateException("Windows key store not supported", e);
          }
        } else {
            if (trustStorePath != null) {
                factory.setTrustStorePath(trustStorePath);
            }
            if (trustStorePassword != null) {
                factory.setTrustStorePassword(trustStorePassword);
            }
            factory.setTrustStoreType(trustStoreType);
        }

        if (trustStoreProvider != null) {
            factory.setTrustStoreProvider(trustStoreProvider);
        }

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

        factory.setRenegotiationAllowed(allowRenegotiation);
        factory.setEndpointIdentificationAlgorithm(endpointIdentificationAlgorithm);

        factory.setValidateCerts(validateCerts);
        factory.setValidatePeerCerts(validatePeers);

        if (supportedProtocols != null) {
            factory.setIncludeProtocols(Iterables.toArray(supportedProtocols, String.class));
        }

        if (excludedProtocols != null) {
            factory.setExcludeProtocols(Iterables.toArray(excludedProtocols, String.class));
        }

        if (supportedCipherSuites != null) {
            factory.setIncludeCipherSuites(Iterables.toArray(supportedCipherSuites, String.class));
        }

        if (excludedCipherSuites != null) {
            factory.setExcludeCipherSuites(Iterables.toArray(excludedCipherSuites, String.class));
        }

        return factory;
    }
}
