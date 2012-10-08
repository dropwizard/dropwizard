package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.util.Size;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public class GzipConfiguration {
    @JsonProperty
    protected boolean enabled = true;

    @JsonProperty
    protected Size minimumEntitySize = null;

    @JsonProperty
    protected Size bufferSize = null;

    @JsonProperty
    protected ImmutableSet<String> excludedUserAgents = null;

    @JsonProperty
    protected ImmutableSet<String> compressedMimeTypes = null;

    public boolean isEnabled() {
        return enabled;
    }

    public Optional<Size> getMinimumEntitySize() {
        return Optional.fromNullable(minimumEntitySize);
    }

    public Optional<Size> getBufferSize() {
        return Optional.fromNullable(bufferSize);
    }

    public Optional<ImmutableSet<String>> getExcludedUserAgents() {
        return Optional.fromNullable(excludedUserAgents);
    }

    public Optional<ImmutableSet<String>> getCompressedMimeTypes() {
        return Optional.fromNullable(compressedMimeTypes);
    }
}
