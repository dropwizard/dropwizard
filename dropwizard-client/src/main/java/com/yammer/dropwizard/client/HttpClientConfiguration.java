package com.yammer.dropwizard.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.util.Duration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * The configuration class used by {@link HttpClientBuilder}.
 * <h1>Properties</h1>
 * <table>
 *     <tr>
 *         <td>Property Name</td>
 *         <td>Required</td>
 *         <td>Description</td>
 *         <td>Default Value</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeout}</td>
 *         <td>No</td>
 *         <td>
 *             The socket timeout value. If a read or write to the underlying TCP/IP connection
 *             hasn't succeeded after this duration, a timeout exception is thrown.
 *         </td>
 *         <td>500ms</td>
 *     </tr>
 *     <tr>
 *         <td>{@code connectionTimeout}</td>
 *         <td>No</td>
 *         <td>
 *             The connection timeout value. If a TCP/IP connection cannot be established in this
 *             time, a timeout exception is thrown.
 *         </td>
 *         <td>500ms</td>
 *     </tr>
 *     <tr>
 *         <td>{@code timeToLive}</td>
 *         <td>No</td>
 *         <td>
 *             The time a TCP/IP connection to the server is allowed to persist before being
 *             explicitly closed.
 *         </td>
 *         <td>1 hour</td>
 *     </tr>
 *     <tr>
 *         <td>{@code cookiesEnabled}</td>
 *         <td>No</td>
 *         <td>
 *             If {@code true}, cookies will be persisted in memory for the duration of the client's
 *             lifetime. If {@code false}, cookies will be ignored entirely.
 *         </td>
 *         <td>{@code false}</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxConnections}</td>
 *         <td>No</td>
 *         <td>
 *             The maximum number of connections to be held in the client's connection pool.
 *         </td>
 *         <td>1024</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxConnectionsPerRoute}</td>
 *         <td>No</td>
 *         <td>
 *             The maximum number of connections per "route" to be held in the client's connection
 *             pool. A route is essentially a combination of hostname, port, configured proxies,
 *             etc.
 *         </td>
 *         <td>1024</td>
 *     </tr>
 *     <tr>
 *         <td>{@code keepAlive}</td>
 *         <td>No</td>
 *         <td>
 *             The default value for a persistent connection's keep-alive. A value of {@code 0} will
 *             result in connections being immediately closed after a response.
 *         </td>
 *         <td>0ms</td>
 *     </tr>
 * </table>
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
