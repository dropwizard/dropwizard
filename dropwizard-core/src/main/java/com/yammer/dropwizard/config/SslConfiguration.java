package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.codehaus.jackson.annotate.JsonProperty;

public class SslConfiguration {
    @JsonProperty
    protected String keyStorePath = null;

    @JsonProperty
    protected String keyStorePassword = null;

    @JsonProperty
    protected String keyManagerPassword = null;

    @JsonProperty
    protected ImmutableList<String> includeProtocols = null;

    public Optional<String> getKeyStorePath() {
        return Optional.fromNullable(keyStorePath);
    }

    public Optional<String> getKeyStorePassword() {
        return Optional.fromNullable(keyStorePassword);
    }

    public Optional<String> getKeyManagerPassword() {
        return Optional.fromNullable(keyManagerPassword);
    }

    public Optional<ImmutableList<String>> getIncludeProtocols() {
        return Optional.fromNullable(includeProtocols);
    }
}
