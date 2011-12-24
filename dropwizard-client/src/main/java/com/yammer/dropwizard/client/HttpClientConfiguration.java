package com.yammer.dropwizard.client;

import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class HttpClientConfiguration {
    @NotNull
    private Duration timeout = Duration.milliseconds(500);

    @NotNull
    private Duration timeToLive = Duration.hours(1);

    private boolean cookiesEnabled = false;

    @Max(Integer.MAX_VALUE)
    @Min(1)
    private int maxConnections = 1024;

    public Duration getTimeout() {
        return timeout;
    }

    public Duration getTimeToLive() {
        return timeToLive;
    }

    public boolean isCookiesEnabled() {
        return cookiesEnabled;
    }

    public void setTimeout(Duration duration) {
        this.timeout = duration;
    }

    public void setTimeToLive(Duration timeToLive) {
        this.timeToLive = timeToLive;
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
