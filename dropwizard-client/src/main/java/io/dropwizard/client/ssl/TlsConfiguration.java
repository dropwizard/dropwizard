package io.dropwizard.client.ssl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;

public class TlsConfiguration {

    @NotEmpty
    private String protocol = "TLSv1.2";

    private File keyStorePath;

    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    private File trustStorePath;

    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    private boolean trustSelfSignedCertificates = false;

    private boolean verifyHostname = true;

    @Nullable
    private List<String> supportedProtocols = null;

    @Nullable
    private List<String> supportedCiphers = null;

    @JsonProperty
    public void setTrustSelfSignedCertificates(boolean trustSelfSignedCertificates) {
        this.trustSelfSignedCertificates = trustSelfSignedCertificates;
    }

    @JsonProperty
    public boolean isTrustSelfSignedCertificates() {
        return trustSelfSignedCertificates;
    }

    @JsonProperty
    public File getKeyStorePath() {
        return keyStorePath;
    }

    @JsonProperty
    public void setKeyStorePath(File keyStorePath) {
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
    public String getTrustStoreType() {
        return trustStoreType;
    }

    @JsonProperty
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }


    @JsonProperty
    public File getTrustStorePath() {
        return trustStorePath;
    }

    @JsonProperty
    public void setTrustStorePath(File trustStorePath) {
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

    @ValidationMethod(message = "keyStorePassword should not be null or empty if keyStorePath not null")
    public boolean isValidKeyStorePassword() {
        return keyStorePath == null || keyStoreType.startsWith("Windows-") || !Strings.isNullOrEmpty(keyStorePassword);
    }

    @ValidationMethod(message = "trustStorePassword should not be null or empty if trustStorePath not null")
    public boolean isValidTrustStorePassword() {
        return trustStorePath == null || trustStoreType.startsWith("Windows-") || !Strings.isNullOrEmpty(trustStorePassword);
    }
}