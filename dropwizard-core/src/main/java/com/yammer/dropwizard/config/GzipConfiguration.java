package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.util.Size;

import java.util.Set;

@SuppressWarnings("UnusedDeclaration")
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

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Optional<Size> getMinimumEntitySize() {
        return Optional.fromNullable(minimumEntitySize);
    }

    public void setMinimumEntitySize(Size size) {
        this.minimumEntitySize = size;
    }

    public Optional<Size> getBufferSize() {
        return Optional.fromNullable(bufferSize);
    }

    public void setBufferSize(Size size) {
        this.bufferSize = size;
    }

    public Optional<ImmutableSet<String>> getExcludedUserAgents() {
        return Optional.fromNullable(excludedUserAgents);
    }

    public void setExcludedUserAgents(Set<String> userAgents) {
        this.excludedUserAgents = (userAgents == null) ? null : ImmutableSet.copyOf(userAgents);
    }

    public Optional<ImmutableSet<String>> getCompressedMimeTypes() {
        return Optional.fromNullable(compressedMimeTypes);
    }

    public void setCompressedMimeTypes(Set<String> mimeTypes) {
        this.compressedMimeTypes = (mimeTypes == null) ? null : ImmutableSet.copyOf(mimeTypes);
    }
}
