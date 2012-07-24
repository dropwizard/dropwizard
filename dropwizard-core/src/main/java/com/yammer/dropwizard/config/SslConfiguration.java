package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class SslConfiguration {
    @JsonProperty
    protected String keyStorePath = null;

    @JsonProperty
    protected String keyStorePassword = null;

    @JsonProperty
    protected String keyManagerPassword = null;

    @JsonProperty
    private String keyStoreType = "JKS";

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

    public String[] getSupportedProtocols() {
        return supportedProtocols.toArray(new String[supportedProtocols.size()]);
    }
}
