package com.yammer.dropwizard.config;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.util.Size;
import org.codehaus.jackson.annotate.JsonProperty;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public class GzipConfiguration {
    @JsonProperty
    private boolean enabled = true;

    @JsonProperty
    private Size minimumEntitySize = null;

    @JsonProperty
    private Size bufferSize = null;

    @JsonProperty
    private ImmutableSet<String> excludedUserAgents = null;

    @JsonProperty
    private ImmutableSet<String> compressedMimeTypes = null;

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
