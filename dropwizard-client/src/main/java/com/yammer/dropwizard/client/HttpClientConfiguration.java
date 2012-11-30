package com.yammer.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * The configuration class used by {@link HttpClientBuilder}.
 *
  * @see <a href="http://dropwizard.codahale.com/manual/client.html#configuration-defaults">Http Client Configuration</a>
 */
public class HttpClientConfiguration {
    @NotNull
    @JsonProperty
    private Duration timeout = Duration.milliseconds(500);

    @NotNull
    @JsonProperty
    private Duration connectionTimeout = Duration.milliseconds(500);

    @NotNull
    @JsonProperty
    private Duration timeToLive = Duration.hours(1);

    @JsonProperty
    private boolean cookiesEnabled = false;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    @JsonProperty
    private int maxConnections = 1024;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    @JsonProperty
    private int maxConnectionsPerRoute = 1024;

    @NotNull
    @JsonProperty
    private Duration keepAlive = Duration.milliseconds(0);

    public Duration getKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(Duration keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getMaxConnectionsPerRoute() {
        return maxConnectionsPerRoute;
    }

    public void setMaxConnectionsPerRoute(int maxConnectionsPerRoute) {
        this.maxConnectionsPerRoute = maxConnectionsPerRoute;
    }
    public Duration getTimeout() {
        return timeout;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
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

    public void setConnectionTimeout(Duration duration) {
        this.connectionTimeout = duration;
    }

    @SuppressWarnings("UnusedDeclaration") // sadly, no real way to test this
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
