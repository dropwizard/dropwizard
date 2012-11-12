package com.yammer.dropwizard.config;

import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class SslConfiguration {
    @JsonProperty
    protected String keyStorePath = null;

    @JsonProperty
    protected String keyStorePassword = null;

    @JsonProperty
    protected String keyManagerPassword = null;

    @JsonProperty
    private String keyStoreType = "JKS";

    @JsonProperty
    protected String trustStorePath = null;
    
    @JsonProperty
    protected String trustStorePassword = null;
    
    @JsonProperty
    private String trustStoreType = "JKS";
    
    @JsonProperty
    private Boolean needClientAuth = false;

    @NotEmpty
    @JsonProperty
    protected ImmutableList<String> supportedProtocols = ImmutableList.of("SSLv3",
                                                                          "TLSv1",
                                                                          "TLSv1.1",
                                                                          "TLSv1.2");

    public Optional<String> getKeyStorePath() {
        return Optional.fromNullable(keyStorePath);
    }

    public Optional<String> getKeyStorePassword() {
        return Optional.fromNullable(keyStorePassword);
    }

    public Optional<String> getKeyManagerPassword() {
        return Optional.fromNullable(keyManagerPassword);
    }

    public Optional<String> getKeyStoreType() {
        return Optional.fromNullable(keyStoreType);
    }

    public Optional<String> getTrustStorePath() {
        return Optional.fromNullable(trustStorePath);
    }
    
    public Optional<String> getTrustStorePassword() {
        return Optional.fromNullable(trustStorePassword);
    }
    
    public Optional<String> getTrustStoreType() {
        return Optional.fromNullable(trustStoreType);
    }

    public Optional<Boolean> getNeedClientAuth() {
        return Optional.fromNullable(needClientAuth);
    }
    
    public String[] getSupportedProtocols() {
        return supportedProtocols.toArray(new String[supportedProtocols.size()]);
    }
}
