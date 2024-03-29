package io.dropwizard.client.ssl;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.validation.ValidationMethod;
import jakarta.validation.constraints.NotEmpty;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.File;
import java.util.List;
import java.util.Optional;

public class TlsConfiguration {

    @NotEmpty
    private String protocol = "TLSv1.2";

    @Nullable
    private String provider;

    @Nullable
    private File keyStorePath;

    @Nullable
    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    @Nullable
    private String keyStoreProvider;

    @Nullable
    private File trustStorePath;

    @Nullable
    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    @Nullable
    private String trustStoreProvider;

    private boolean trustSelfSignedCertificates = false;

    private boolean verifyHostname = true;

    @Nullable
    private List<String> supportedProtocols = null;

    @Nullable
    private List<String> supportedCiphers = null;

    @Nullable
    private String certAlias = null;

    @JsonProperty
    public void setTrustSelfSignedCertificates(boolean trustSelfSignedCertificates) {
        this.trustSelfSignedCertificates = trustSelfSignedCertificates;
    }

    @JsonProperty
    public boolean isTrustSelfSignedCertificates() {
        return trustSelfSignedCertificates;
    }

    @JsonProperty
    @Nullable
    public File getKeyStorePath() {
        return keyStorePath;
    }

    @JsonProperty
    public void setKeyStorePath(File keyStorePath) {
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
    public String getTrustStoreType() {
        return trustStoreType;
    }

    @JsonProperty
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    @JsonProperty
    @Nullable
    public File getTrustStorePath() {
        return trustStorePath;
    }

    @JsonProperty
    public void setTrustStorePath(File trustStorePath) {
        this.trustStorePath = trustStorePath;
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
    public boolean isVerifyHostname() {
        return verifyHostname;
    }

    @JsonProperty
    public void setVerifyHostname(boolean verifyHostname) {
        this.verifyHostname = verifyHostname;
    }

    @JsonProperty
    public String getProtocol() {
        return protocol;
    }

    @JsonProperty
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @JsonProperty
    @Nullable
    public String getProvider() {
        return provider;
    }

    @JsonProperty
    public void setProvider(@Nullable String provider) {
        this.provider = provider;
    }

    @Nullable
    @JsonProperty
    public List<String> getSupportedCiphers() {
        return supportedCiphers;
    }

    @JsonProperty
    public void setSupportedCiphers(@Nullable List<String> supportedCiphers) {
        this.supportedCiphers = supportedCiphers;
    }

    @Nullable
    @JsonProperty
    public List<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    @JsonProperty
    public void setSupportedProtocols(@Nullable List<String> supportedProtocols) {
        this.supportedProtocols = supportedProtocols;
    }

    @Nullable
    @JsonProperty
    public String getCertAlias() {
        return certAlias;
    }

    @JsonProperty
    public void setCertAlias(@Nullable String certAlias) {
        this.certAlias = certAlias;
    }

    @ValidationMethod(message = "keyStorePassword should not be null or empty if keyStorePath not null")
    public boolean isValidKeyStorePassword() {
        return keyStorePath == null
            || keyStoreType.startsWith("Windows-")
            || Optional.ofNullable(keyStorePassword).filter(s -> !s.isEmpty()).isPresent();
    }

    @ValidationMethod(message = "trustStorePassword should not be null or empty if trustStorePath not null")
    public boolean isValidTrustStorePassword() {
        return trustStorePath == null
            || trustStoreType.startsWith("Windows-")
            || Optional.ofNullable(trustStorePassword).filter(s -> !s.isEmpty()).isPresent();
    }

    /**
     * @since 2.0
     */
    @Nullable
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    /**
     * @since 2.0
     */
    public void setKeyStoreProvider(@Nullable String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    /**
     * @since 2.0
     */
    @Nullable
    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    /**
     * @since 2.0
     */
    public void setTrustStoreProvider(@Nullable String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }
}
