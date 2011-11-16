package com.yammer.dropwizard.client;

import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public class HttpClientConfiguration {
    @NotNull
    @Pattern(regexp = Duration.VALID_DURATION)
    private String timeout = "500ms";

    @NotNull
    @Pattern(regexp = Duration.VALID_DURATION)
    private String timeToLive = "1 hour";

    private boolean cookiesEnabled = false;

    @Max(Integer.MAX_VALUE)
    @Min(1)
    private int maxConnections = 1024;

    public Duration getTimeout() {
        return Duration.parse(timeout);
    }

    public Duration getTimeToLive() {
        return Duration.parse(timeToLive);
    }

    public boolean isCookiesEnabled() {
        return cookiesEnabled;
    }

    public void setTimeout(Duration duration) {
        this.timeout = duration.toString();
    }

    public void setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive.toString();
    }

    public void setCookiesEnabled(boolean enabled) {
        this.cookiesEnabled = enabled;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }
}
