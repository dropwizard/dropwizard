package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 */
public class SslConfiguration {
    @JsonProperty
    private String keyStorePath = null;

    @JsonProperty
    private String keyStorePassword = null;

    @JsonProperty
    private String keyManagerPassword = null;

    public boolean isDefaultKeyStore()
    {
        return keyStorePath == null;
    }

    public Optional<String> getKeyStorePath() {
        return Optional.fromNullable(keyStorePath);
    }

    public Optional<String> getKeyStorePassword() {
        return Optional.fromNullable(keyStorePassword);
    }

    public Optional<String> getKeyManagerPassword() {
        return Optional.fromNullable(keyManagerPassword);
    }
}
