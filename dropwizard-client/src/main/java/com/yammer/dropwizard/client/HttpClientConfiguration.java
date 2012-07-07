package com.yammer.dropwizard.client;

import com.yammer.dropwizard.util.Duration;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkArgument;

public class HttpClientConfiguration {

    public static class DnsOverrides extends HashMap<String, String> {

        @Override
        public String put(String key, String value) {
            checkArgument(isValidInetAddress(value), "%s is not a valid inet address", value);
            return super.put(key, value);
        }

        public InetAddress lookUp(String name) throws UnknownHostException {
            return InetAddress.getByName(get(name));
        }

        private boolean isValidInetAddress(String value) {
            try {
                InetAddress.getByName(value);
            } catch (Exception e) {
                return false;
            }

            return true;
        }
    }

    @Valid
    @JsonProperty
    @NotNull
    private DnsOverrides dnsOverrides = new DnsOverrides();

    @NotNull
    private Duration timeout = Duration.milliseconds(500);

    @NotNull
    private Duration connectionTimeout = Duration.milliseconds(500);

    @NotNull
    private Duration timeToLive = Duration.hours(1);

    private boolean cookiesEnabled = false;

    @Max(Integer.MAX_VALUE)
    @Min(1)
    private int maxConnections = 1024;

    @NotNull
    private Duration keepAlive = Duration.milliseconds(0l);

    @Max(Integer.MAX_VALUE)
    @Min(1)
    private int maxConnectionsPerRoute = 1024;

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

    public DnsOverrides getDnsOverrides() {
        return dnsOverrides;
    }
}
