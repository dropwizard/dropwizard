package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.util.Size;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import org.eclipse.jetty.server.Handler;

import javax.validation.constraints.NotNull;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class GzipHandlerFactory {
    private boolean enabled = true;

    @NotNull
    private Size minimumEntitySize = Size.bytes(256);

    @NotNull
    private Size bufferSize = Size.kilobytes(8);

    @NotNull
    private ImmutableSet<String> excludedUserAgents = ImmutableSet.of();

    @NotNull
    private ImmutableSet<String> compressedMimeTypes = ImmutableSet.of();

    @JsonProperty
    public boolean isEnabled() {
        return enabled;
    }

    @JsonProperty
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty
    public Size getMinimumEntitySize() {
        return minimumEntitySize;
    }

    @JsonProperty
    public void setMinimumEntitySize(Size size) {
        this.minimumEntitySize = checkNotNull(size);
    }

    @JsonProperty
    public Size getBufferSize() {
        return bufferSize;
    }

    @JsonProperty
    public void setBufferSize(Size size) {
        this.bufferSize = checkNotNull(size);
    }

    @JsonProperty
    public ImmutableSet<String> getExcludedUserAgents() {
        return excludedUserAgents;
    }

    @JsonProperty
    public void setExcludedUserAgents(Set<String> userAgents) {
        this.excludedUserAgents = ImmutableSet.copyOf(userAgents);
    }

    @JsonProperty
    public ImmutableSet<String> getCompressedMimeTypes() {
        return compressedMimeTypes;
    }

    @JsonProperty
    public void setCompressedMimeTypes(Set<String> mimeTypes) {
        this.compressedMimeTypes = ImmutableSet.copyOf(mimeTypes);
    }

    public Handler wrapHandler(Handler handler) {
        if (enabled) {
            final BiDiGzipHandler gzipHandler = new BiDiGzipHandler(handler);

            gzipHandler.setMinGzipSize((int) minimumEntitySize.toBytes());

            gzipHandler.setBufferSize((int) bufferSize.toBytes());

            if (!excludedUserAgents.isEmpty()) {
                gzipHandler.setExcluded(excludedUserAgents);
            }

            if (!compressedMimeTypes.isEmpty()) {
                gzipHandler.setMimeTypes(compressedMimeTypes);
            }

            return gzipHandler;
        }

        return handler;
    }
}
