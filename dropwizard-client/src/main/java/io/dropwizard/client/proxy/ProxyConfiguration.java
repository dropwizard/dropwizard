package io.dropwizard.client.proxy;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import io.dropwizard.validation.OneOf;
import org.hibernate.validator.constraints.NotEmpty;
import org.hibernate.validator.constraints.Range;

import javax.annotation.Nullable;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * Configuration of access to a remote host through a proxy server
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code host}</td>
 *         <td>REQUIRED</td>
 *         <td>The proxy server host name or ip address.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code port}</td>
 *         <td>scheme default</td>
 *         <td>The proxy server port. If the port is not set then the scheme default port is used.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code scheme}</td>
 *         <td>http</td>
 *         <td>The proxy server URI scheme. HTTP and HTTPS schemas are permitted. By default HTTP scheme is used.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code auth}</td>
 *         <td>(none)</td>
 *         <td>
 *             The proxy server {@link io.dropwizard.client.proxy.AuthConfiguration} BASIC authentication credentials.
 *             If they are not set then no credentials will be passed to the server.
 *         </td>
 *     </tr>
 * </table>
 */
public class ProxyConfiguration {

    private static final String DEFAULT_SCHEME = "http";
    private static final int DEFAULT_PORT = -1;

    @NotEmpty
    private String host;

    @Range(min = 1, max = 65535)
    @Nullable
    private Integer port;

    @OneOf({"http", "https", "HTTP", "HTTPS"})
    @Nullable
    private String scheme;

    @Valid
    @Nullable
    private AuthConfiguration auth;

    public ProxyConfiguration() {
    }

    public ProxyConfiguration(@NotNull String host, Optional<Integer> port, Optional<String> scheme, Optional<AuthConfiguration> auth) {
        this.host = host;
        this.port = port.or(DEFAULT_PORT);
        this.scheme = scheme.or(DEFAULT_SCHEME);
        this.auth = auth.orNull();
    }

    public ProxyConfiguration(@NotNull String host, Optional<Integer> port) {
        this.host = host;
        this.port = port.or(DEFAULT_PORT);
    }

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public Integer getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(Integer port) {
        this.port = port;
    }

    public int getPresentPort() {
        return Objects.firstNonNull(port, DEFAULT_PORT);
    }

    @JsonProperty
    public String getScheme() {
        return scheme;
    }

    @JsonProperty
    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @NotNull
    public String getPresentScheme() {
        return Objects.firstNonNull(scheme, DEFAULT_SCHEME);
    }

    public AuthConfiguration getAuth() {
        return auth;
    }

    public void setAuth(AuthConfiguration auth) {
        this.auth = auth;
    }
}
