package io.dropwizard.logging;

import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Iterables;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a TCP socket
 * secured by the TLS/SSL protocol on the presentation layer.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 * <tr>
 * <td>Name</td>
 * <td>Default</td>
 * <td>Description</td>
 * </tr>
 * <tr>
 * <td>{@code keyStorePath}</td>
 * <td>(none)</td>
 * <td>
 * The path to the Java key store which contains the host certificate and private key.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code keyStorePassword}</td>
 * <td>(none)</td>
 * <td>
 * The password used to access the key store.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code keyStoreType}</td>
 * <td>{@code JKS}</td>
 * <td>
 * The type of key store (usually {@code JKS}, {@code PKCS12}, {@code JCEKS},
 * {@code Windows-MY}, or {@code Windows-ROOT}).
 * </td>
 * </tr>
 * <tr>
 * <td>{@code keyStoreProvider}</td>
 * <td>(none)</td>
 * <td>
 * The JCE provider to use to access the key store.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code trustStorePath}</td>
 * <td>(none)</td>
 * <td>
 * The path to the Java key store which contains the CA certificates used to establish
 * trust.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code trustStorePassword}</td>
 * <td>(none)</td>
 * <td>The password used to access the trust store.</td>
 * </tr>
 * <tr>
 * <td>{@code trustStoreType}</td>
 * <td>{@code JKS}</td>
 * <td>
 * The type of trust store (usually {@code JKS}, {@code PKCS12}, {@code JCEKS},
 * {@code Windows-MY}, or {@code Windows-ROOT}).
 * </td>
 * </tr>
 * <tr>
 * <td>{@code trustStoreProvider}</td>
 * <td>(none)</td>
 * <td>
 * The JCE provider to use to access the trust store.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code jceProvider}</td>
 * <td>(none)</td>
 * <td>The name of the JCE provider to use for cryptographic support.</td>
 * </tr>
 * <tr>
 * <td>{@code validateCerts}</td>
 * <td>false</td>
 * <td>
 * Whether or not to validate TLS certificates before starting. If enabled, Dropwizard
 * will refuse to start with expired or otherwise invalid certificates.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code validatePeers}</td>
 * <td>false</td>
 * <td>
 * Whether or not to validate TLS peer certificates.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code supportedProtocols}</td>
 * <td>JVM default</td>
 * <td>
 * A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are supported. All
 * other protocols will be refused.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code excludedProtocols}</td>
 * <td>[SSL, SSLv2, SSLv2Hello, SSLv3]</td>
 * <td>
 * A list of protocols (e.g., {@code SSLv3}, {@code TLSv1}) which are excluded. These
 * protocols will be refused.
 * </td>
 * </tr>
 * <tr>
 * <td>{@code supportedCipherSuites}</td>
 * <td>JVM default</td>
 * <td>
 * A list of cipher suites (e.g., {@code TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256}) which
 * are supported. All other cipher suites will be refused
 * </td>
 * </tr>
 * <tr>
 * <td>{@code excludedCipherSuites}</td>
 * <td>[.*_(MD5|SHA|SHA1)$]</td>
 * <td>
 * A list of cipher suites (e.g., {@code TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256}) which
 * are excluded. These cipher suites will be refused.
 * </td>
 * </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link TcpSocketAppenderFactory}.
 *
 * @see TcpSocketAppenderFactory
 */
@JsonTypeName("tls")
public class TlsSocketAppenderFactory<E extends DeferredProcessingAware> extends TcpSocketAppenderFactory<E> {

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
    private String jceProvider;

    @Nullable
    private List<String> supportedProtocols;
    @Nullable
    private List<String> excludedProtocols;

    @Nullable
    private List<String> supportedCipherSuites;
    @Nullable
    private List<String> excludedCipherSuites;

    private boolean validateCerts;
    private boolean validatePeers;

    @JsonProperty
    public boolean isValidatePeers() {
        return validatePeers;
    }

    @JsonProperty
    public void setValidatePeers(boolean validatePeers) {
        this.validatePeers = validatePeers;
    }

    @JsonProperty
    public boolean isValidateCerts() {
        return validateCerts;
    }

    @JsonProperty
    public void setValidateCerts(boolean validateCerts) {
        this.validateCerts = validateCerts;
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
    @Nullable
    public List<String> getSupportedCipherSuites() {
        return supportedCipherSuites;
    }

    @JsonProperty
    public void setSupportedCipherSuites(List<String> supportedCipherSuites) {
        this.supportedCipherSuites = supportedCipherSuites;
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
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @JsonProperty
    public void setSupportedProtocols(List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
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
    public String getTrustStoreType() {
        return trustStoreType;
    }

    @JsonProperty
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @JsonProperty
    @Nullable
    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    @JsonProperty
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
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
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    @JsonProperty
    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    @JsonProperty
    @Nullable
    public String getKeyStoreType() {
        return keyStoreType;
    }

    @JsonProperty
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
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
    public String getJceProvider() {
        return jceProvider;
    }

    @JsonProperty
    public void setJceProvider(String jceProvider) {
        this.jceProvider = jceProvider;
    }

    private SslContextFactory createSslContextFactory() {
        SslContextFactory factory = new SslContextFactory();
        if (keyStorePath != null) {
            factory.setKeyStorePath(keyStorePath);
        }
        factory.setKeyStoreType(keyStoreType);
        if (keyStorePassword != null) {
            factory.setKeyStorePassword(keyStorePassword);
        }
        if (keyStoreProvider != null) {
            factory.setKeyStoreProvider(keyStoreProvider);
        }
        if (trustStorePath != null) {
            factory.setTrustStorePath(trustStorePath);
        }
        if (trustStorePassword != null) {
            factory.setTrustStorePassword(trustStorePassword);
        }
        factory.setTrustStoreType(trustStoreType);
        if (trustStoreProvider != null) {
            factory.setTrustStoreProvider(trustStoreProvider);
        }
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
        if (jceProvider != null) {
            factory.setProvider(jceProvider);
        }
        return factory;
    }

    @Override
    protected SocketFactory socketFactory() {
        final SslContextFactory sslContextFactory = createSslContextFactory();
        try {
            sslContextFactory.start();
        } catch (Exception e) {
            throw new IllegalStateException("Unable to configure SSLContext", e);
        }
        // We use an adapter over the `newSslSocket` call of Jetty's `SslContextFactory`, because it provides more
        // advanced socket configuration than Java's `SSLSocketFactory`.
        return new SocketFactory() {
            @Override
            public Socket createSocket() throws IOException {
                return sslContextFactory.newSslSocket();
            }

            @Override
            public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
                return unsupported();
            }

            @Override
            public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
                return unsupported();
            }

            @Override
            public Socket createSocket(InetAddress host, int port) throws IOException {
                return unsupported();
            }

            @Override
            public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
                return unsupported();
            }

            private Socket unsupported() {
                throw new UnsupportedOperationException("Only createSocket is supported");
            }
        };
    }


}
