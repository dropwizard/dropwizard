package io.dropwizard.client.ssl;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import io.dropwizard.validation.ValidationMethod;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.File;

public class TlsConfiguration {

    @NotEmpty
    private String protocol = "TLS";

    private File keyStorePath;

    private String keyStorePassword;

    @NotEmpty
    private String keyStoreType = "JKS";

    private File trustStorePath;

    private String trustStorePassword;

    @NotEmpty
    private String trustStoreType = "JKS";

    private boolean trustSelfSignedCertificates = false;

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
    public String getProtocol() {
        return protocol;
    }

    @JsonProperty
    public void setProtocol(String protocol) {
        this.protocol = protocol;
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