package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;

public class ServletConfiguration {

    private boolean enabled = true;

    @Nullable
    private String uri;

    @JsonProperty("enabled")
    public boolean isEnabled() {
        return enabled;
    }

    public String isEnabledString() {
        return Boolean.toString(enabled);
    }

    @JsonProperty("enabled")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("uri")
    @Nullable
    public String getUri() {
        return uri;
    }

    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }
}
