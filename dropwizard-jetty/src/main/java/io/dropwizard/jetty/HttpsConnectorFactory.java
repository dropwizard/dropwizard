package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.util.Strings;
import io.dropwizard.validation.ValidationMethod;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.net.ssl.SSLEngine;
import java.io.File;
import java.net.URI;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
 *         <td>false</td>
 *         <td>
 *             Whether or not to validate TLS certificates before starting. If enabled, Dropwizard
 *             will refuse to start with expired or otherwise invalid certificates. This option will
 *             cause unconditional failure in Dropwizard 1.x until a new validation mechanism can be
 *             implemented.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code validatePeers}</td>
 *         <td>false</td>
 *         <td>
 *             Whether or not to validate TLS peer certificates. This option will
 *             cause unconditional failure in Dropwizard 1.x until a new validation mechanism can be
 *             implemented.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code supportedProtocols}</td>
 *         <td>JVM default</td>
 *         <td>
 *             A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are supported. All
 *             other protocols will be refused.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code excludedProtocols}</td>
 *         <td>["SSL.*", "TLSv1", "TLSv1\.1"]</td>
 *         <td>
 *             A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are excluded. These
 *             protocols will be refused.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code supportedCipherSuites}</td>
 *         <td>JVM default</td>
 *         <td>
 *             A list of cipher suites (e.g., {@code TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256}) which
 *             are supported. All other cipher suites will be refused
 *         </td>
 *    </tr>
 *    <tr>
 *         <td>{@code excludedCipherSuites}</td>
 *         <td>Jetty's default</td>
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

    @Nullable
    private String keyStorePath;

    @Nullable
    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    @Nullable
    private String keyStoreProvider;

    @Nullable
    private String trustStorePath;

    @Nullable
    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    @Nullable
    private String trustStoreProvider;

    @Nullable
    private String keyManagerPassword;

    @Nullable
    private Boolean needClientAuth;

    @Nullable
    private Boolean wantClientAuth;

    @Nullable
    private String certAlias;

    @Nullable
    private File crlPath;

    @Nullable
    private Boolean enableCRLDP;

    @Nullable
    private Boolean enableOCSP;

    @Nullable
    private Integer maxCertPathLength;

    @Nullable
    private URI ocspResponderUrl;

    @Nullable
    private String jceProvider;
    private boolean validateCerts = false;
    private boolean validatePeers = false;

    @Nullable
    private List<String> supportedProtocols;

    @Nullable
    private List<String> excludedProtocols = Arrays.asList("SSL.*", "TLSv1", "TLSv1\\.1");

    @Nullable
    private List<String> supportedCipherSuites;

    @Nullable
    private List<String> excludedCipherSuites;

    private boolean allowRenegotiation = true;

    @Nullable
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
    @Nullable
    public String getEndpointIdentificationAlgorithm() {
        return endpointIdentificationAlgorithm;
    }

    @JsonProperty
    public void setEndpointIdentificationAlgorithm(String endpointIdentificationAlgorithm) {
        this.endpointIdentificationAlgorithm = endpointIdentificationAlgorithm;
    }

    @JsonProperty
    @Nullable
    public String getKeyStorePath() {
        return keyStorePath;
    }

    @JsonProperty
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    @JsonProperty
    @Nullable
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
    @Nullable
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
    @Nullable
    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    @JsonProperty
    public void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    @JsonProperty
    @Nullable
    public String getKeyManagerPassword() {
        return keyManagerPassword;
    }

    @JsonProperty
    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    @JsonProperty
    @Nullable
    public String getTrustStorePath() {
        return trustStorePath;
    }

    @JsonProperty
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    @JsonProperty
    @Nullable
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    @JsonProperty
    public void setTrustStorePassword(@Nullable String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    @JsonProperty
    @Nullable
    public Boolean getNeedClientAuth() {
        return needClientAuth;
    }

    @JsonProperty
    public void setNeedClientAuth(Boolean needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    @JsonProperty
    @Nullable
    public Boolean getWantClientAuth() {
        return wantClientAuth;
    }

    @JsonProperty
    public void setWantClientAuth(Boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

    @JsonProperty
    @Nullable
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty
    public void setCertAlias(String certAlias) {
        this.certAlias = certAlias;
    }

    @JsonProperty
    @Nullable
    public File getCrlPath() {
        return crlPath;
    }

    @JsonProperty
    public void setCrlPath(File crlPath) {
        this.crlPath = crlPath;
    }

    @JsonProperty
    @Nullable
    public Boolean getEnableCRLDP() {
        return enableCRLDP;
    }

    @JsonProperty
    public void setEnableCRLDP(Boolean enableCRLDP) {
        this.enableCRLDP = enableCRLDP;
    }

    @JsonProperty
    @Nullable
    public Boolean getEnableOCSP() {
        return enableOCSP;
    }

    @JsonProperty
    public void setEnableOCSP(Boolean enableOCSP) {
        this.enableOCSP = enableOCSP;
    }

    @JsonProperty
    @Nullable
    public Integer getMaxCertPathLength() {
        return maxCertPathLength;
    }

    @JsonProperty
    public void setMaxCertPathLength(Integer maxCertPathLength) {
        this.maxCertPathLength = maxCertPathLength;
    }

    @JsonProperty
    @Nullable
    public URI getOcspResponderUrl() {
        return ocspResponderUrl;
    }

    @JsonProperty
    public void setOcspResponderUrl(URI ocspResponderUrl) {
        this.ocspResponderUrl = ocspResponderUrl;
    }

    @JsonProperty
    @Nullable
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
    @Nullable
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @JsonProperty
    public void setSupportedProtocols(List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    @JsonProperty
    @Nullable
    public List<String> getExcludedProtocols() {
        return excludedProtocols;
    }

    @JsonProperty
    public void setExcludedProtocols(List<String> excludedProtocols) {
        this.excludedProtocols = excludedProtocols;
    }

    @JsonProperty
    @Nullable
    public List<String> getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    @JsonProperty
    @Nullable
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
    public Connector build(Server server, MetricRegistry metrics, String name, @Nullable ThreadPool threadPool) {
        final HttpConfiguration httpConfig = buildHttpConfiguration();

        final HttpConnectionFactory httpConnectionFactory = buildHttpConnectionFactory(httpConfig);

        final SslContextFactory sslContextFactory = configureSslContextFactory(new SslContextFactory());
        sslContextFactory.addLifeCycleListener(logSslInfoOnStart(sslContextFactory));

        server.addBean(sslContextFactory);
        server.addBean(new SslReload(sslContextFactory, this::configureSslContextFactory));

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

    /** Register a listener that waits until the ssl context factory has started. Once it has
     *  started we can grab the fully initialized context so we can log the parameters.
     */
    protected AbstractLifeCycle.AbstractLifeCycleListener logSslInfoOnStart(final SslContextFactory sslContextFactory) {
        return new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarted(LifeCycle event) {
                logSupportedParameters(sslContextFactory);
            }
        };
    }

    /**
     * Given a list of protocols available to the JVM that we can serve up to the client, partition
     * this list into two groups: a group of protocols we can serve and a group where we can't. This
     * list takes into account protocols that may have been disabled at the JVM level, and also
     * protocols that the user explicitly wants to include / exclude. The exclude list (blacklist)
     * is stronger than include list (whitelist), so a protocol that is in both lists will be
     * excluded. Other than the initial list of available protocols, the other lists are patterns,
     * such that one can exclude all SSL protocols with a single exclude entry of "SSL.*". This
     * function will handle both cipher suites and protocols, but for the sake of conciseness, this
     * documentation only talks about protocols. This implementation is a slimmed down version from
     * jetty:
     * https://github.com/eclipse/jetty.project/blob/93a8afcc6bd1a6e0af7bd9f967c97ae1bc3eb718/jetty-util/src/main/java/org/eclipse/jetty/util/ssl/SslSelectionDump.java
     *
     * @param supportedByJVM protocols available to the JVM.
     * @param enabledByJVM protocols enabled by lib/security/java.security.
     * @param excludedByConfig protocols the user doesn't want to expose.
     * @param includedByConfig the only protocols the user wants to expose.
     * @return two entry map of protocols that are enabled (true) and those that have been disabled (false).
     */
    static Map<Boolean, List<String>> partitionSupport(
        String[] supportedByJVM,
        String[] enabledByJVM,
        String[] excludedByConfig,
        String[] includedByConfig
    ) {
        final List<Pattern> enabled = Arrays.stream(enabledByJVM).map(Pattern::compile).collect(Collectors.toList());
        final List<Pattern> disabled = Arrays.stream(excludedByConfig).map(Pattern::compile).collect(Collectors.toList());
        final List<Pattern> included = Arrays.stream(includedByConfig).map(Pattern::compile).collect(Collectors.toList());

        return Arrays.stream(supportedByJVM)
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.partitioningBy(x ->
                disabled.stream().noneMatch(pat -> pat.matcher(x).matches()) &&
                    enabled.stream().anyMatch(pat -> pat.matcher(x).matches()) &&
                    (included.isEmpty() || included.stream().anyMatch(pat -> pat.matcher(x).matches()))
            ));

    }

    private void logSupportedParameters(SslContextFactory contextFactory) {
        if (LOGGED.compareAndSet(false, true)) {
            // When Jetty logs out which protocols are enabled / disabled they include tracing
            // information to detect if the protocol was disabled at the
            // JRE/lib/security/java.security level. Since we don't log this information we take the
            // SSLEngine from our context instead of a pristine version.
            //
            // For more info from Jetty:
            // https://github.com/eclipse/jetty.project/blob/93a8afcc6bd1a6e0af7bd9f967c97ae1bc3eb718/jetty-util/src/main/java/org/eclipse/jetty/util/ssl/SslContextFactory.java#L356-L360
            final SSLEngine engine = contextFactory.getSslContext().createSSLEngine();

            final Map<Boolean, List<String>> protocols = partitionSupport(
                engine.getSupportedProtocols(),
                engine.getEnabledProtocols(),
                contextFactory.getExcludeProtocols(),
                contextFactory.getIncludeProtocols()
            );

            final Map<Boolean, List<String>> ciphers = partitionSupport(
                engine.getSupportedCipherSuites(),
                engine.getEnabledCipherSuites(),
                contextFactory.getExcludeCipherSuites(),
                contextFactory.getIncludeCipherSuites()
            );

            LOGGER.info("Enabled protocols: {}", protocols.get(true));
            LOGGER.info("Disabled protocols: {}", protocols.get(false));
            LOGGER.info("Enabled cipher suites: {}", ciphers.get(true));
            LOGGER.info("Disabled cipher suites: {}", ciphers.get(false));
        }
    }

    protected SslContextFactory configureSslContextFactory(SslContextFactory factory) {
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
            factory.setIncludeProtocols(supportedProtocols.toArray(new String[0]));
        }

        if (excludedProtocols != null) {
            factory.setExcludeProtocols(excludedProtocols.toArray(new String[0]));
        }

        if (supportedCipherSuites != null) {
            factory.setIncludeCipherSuites(supportedCipherSuites.toArray(new String[0]));
        }

        if (excludedCipherSuites != null) {
            factory.setExcludeCipherSuites(excludedCipherSuites.toArray(new String[0]));
        }

        return factory;
    }
}
