package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.net.URI;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("UnusedDeclaration")
public class SslConfiguration {
    @NotNull
    @JsonProperty
    private Optional<File> keyStore = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<String> keyStorePassword = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<String> keyManagerPassword = Optional.absent();

    @NotEmpty
    @JsonProperty
    private String keyStoreType = "JKS";

    @NotNull
    @JsonProperty
    private Optional<File> trustStore = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<String> trustStorePassword = Optional.absent();

    @NotEmpty
    @JsonProperty
    private String trustStoreType = "JKS";

    @NotNull
    @JsonProperty
    private Optional<Boolean> needClientAuth = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Boolean> wantClientAuth = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<String> certAlias = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Boolean> allowRenegotiate = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<File> crlPath = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Boolean> crldpEnabled = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Boolean> ocspEnabled = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Integer> maxCertPathLength = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<URI> ocspResponderUrl = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<String> jceProvider = Optional.absent();

    @NotNull
    @JsonProperty
    private Optional<Boolean> validatePeers = Optional.absent();

    @NotEmpty
    @JsonProperty
    private ImmutableList<String> supportedProtocols = ImmutableList.of("SSLv3",
                                                                        "TLSv1",
                                                                        "TLSv1.1",
                                                                        "TLSv1.2");

    public String getKeyStoreType() {
        return keyStoreType;
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = checkNotNull(keyStoreType);
    }

    public ImmutableList<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(List<String> protocols) {
        this.supportedProtocols = ImmutableList.copyOf(protocols);
    }

    public String getTrustStoreType() {
        return trustStoreType;
    }

    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = checkNotNull(trustStoreType);
    }

    public Optional<File> getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(Optional<File> keyStore) {
        this.keyStore = keyStore;
    }

    public Optional<String> getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(Optional<String> keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public Optional<String> getKeyManagerPassword() {
        return keyManagerPassword;
    }

    public void setKeyManagerPassword(Optional<String> keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    public Optional<File> getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(Optional<File> trustStore) {
        this.trustStore = trustStore;
    }

    public Optional<String> getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(Optional<String> trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    public Optional<Boolean> getNeedClientAuth() {
        return needClientAuth;
    }

    public void setNeedClientAuth(Optional<Boolean> needClientAuth) {
        this.needClientAuth = needClientAuth;
    }

    public Optional<Boolean> getWantClientAuth() {
        return wantClientAuth;
    }

    public void setWantClientAuth(Optional<Boolean> wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
    }

    public Optional<String> getCertAlias() {
        return certAlias;
    }

    public void setCertAlias(Optional<String> certAlias) {
        this.certAlias = certAlias;
    }

    public Optional<Boolean> getAllowRenegotiate() {
        return allowRenegotiate;
    }

    public void setAllowRenegotiate(Optional<Boolean> allowRenegotiate) {
        this.allowRenegotiate = allowRenegotiate;
    }

    public Optional<File> getCrlPath() {
        return crlPath;
    }

    public void setCrlPath(Optional<File> crlPath) {
        this.crlPath = crlPath;
    }

    public Optional<Boolean> getCrldpEnabled() {
        return crldpEnabled;
    }

    public void setCrldpEnabled(Optional<Boolean> crldpEnabled) {
        this.crldpEnabled = crldpEnabled;
    }

    public Optional<Boolean> getOcspEnabled() {
        return ocspEnabled;
    }

    public void setOcspEnabled(Optional<Boolean> ocspEnabled) {
        this.ocspEnabled = ocspEnabled;
    }

    public Optional<Integer> getMaxCertPathLength() {
        return maxCertPathLength;
    }

    public void setMaxCertPathLength(Optional<Integer> maxCertPathLength) {
        this.maxCertPathLength = maxCertPathLength;
    }

    public Optional<URI> getOcspResponderUrl() {
        return ocspResponderUrl;
    }

    public void setOcspResponderUrl(Optional<URI> ocspResponderUrl) {
        this.ocspResponderUrl = ocspResponderUrl;
    }

    public Optional<String> getJceProvider() {
        return jceProvider;
    }

    public void setJceProvider(Optional<String> jceProvider) {
        this.jceProvider = jceProvider;
    }

    public Optional<Boolean> getValidatePeers() {
        return validatePeers;
    }

    public void setValidatePeers(Optional<Boolean> validatePeers) {
        this.validatePeers = validatePeers;
    }
}
