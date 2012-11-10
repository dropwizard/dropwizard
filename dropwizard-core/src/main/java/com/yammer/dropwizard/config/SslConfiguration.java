package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.File;
import java.util.List;

@SuppressWarnings("UnusedDeclaration")
public class SslConfiguration {
    @JsonProperty
    private File keyStore = null;

    @JsonProperty
    private String keyStorePassword = null;

    @JsonProperty
    private String keyManagerPassword = null;

    @JsonProperty
    private String keyStoreType = "JKS";

    @JsonProperty
    private String certAlias = null;

    @NotEmpty
    @JsonProperty
    private ImmutableList<String> supportedProtocols = ImmutableList.of("SSLv3",
                                                                        "TLSv1",
                                                                        "TLSv1.1",
                                                                        "TLSv1.2");

    public Optional<File> getKeyStore() {
        return Optional.fromNullable(keyStore);
    }

    public void setKeyStore(File keyStore) {
        this.keyStore = keyStore;
    }

    public Optional<String> getKeyStorePassword() {
        return Optional.fromNullable(keyStorePassword);
    }

    public void setKeyStorePassword(String password) {
        this.keyStorePassword = password;
    }

    public Optional<String> getKeyManagerPassword() {
        return Optional.fromNullable(keyManagerPassword);
    }

    public void setKeyManagerPassword(String keyManagerPassword) {
        this.keyManagerPassword = keyManagerPassword;
    }

    public Optional<String> getKeyStoreType() {
        return Optional.fromNullable(keyStoreType);
    }

    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    public Optional<String> getCertAlias() {
        return Optional.fromNullable(certAlias);
    }

    public void setCertAlias(String alias) {
        this.certAlias = alias;
    }

    public ImmutableList<String> getSupportedProtocols() {
        return supportedProtocols;
    }

    public void setSupportedProtocols(List<String> protocols) {
        this.supportedProtocols = ImmutableList.copyOf(protocols);
    }
}
