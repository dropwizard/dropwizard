package io.dropwizard.health.http;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(HttpHealthCheck.class);

    // visible for testing
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    @Nonnull
    private final String url;
    @Nonnull
    private final Client client;

    public HttpHealthCheck(@Nonnull final String url) {
        this(url, DEFAULT_TIMEOUT, DEFAULT_TIMEOUT);
    }

    public HttpHealthCheck(@Nonnull final String url,
                           final Duration readTimeout,
                           final Duration connectionTimeout) {
        this.url = Objects.requireNonNull(url);
        Preconditions.checkState(readTimeout.toMillis() > 0L);
        Preconditions.checkState(connectionTimeout.toMillis() > 0L);
        this.client = ClientBuilder.newClient()
                .property(ClientProperties.CONNECT_TIMEOUT, (int) connectionTimeout.toMillis())
                .property(ClientProperties.READ_TIMEOUT, (int) readTimeout.toMillis());
    }

    public HttpHealthCheck(@Nonnull final String url,
                           @Nonnull final Client client) {
        this.url = Objects.requireNonNull(url);
        this.client = Objects.requireNonNull(client);
    }

    @Override
    protected Result check() {
        final HttpHealthResponse httpHealthResponse = httpCheck(url);

        if (isHealthResponseValid(httpHealthResponse)) {
            log.debug("Health check against url={} successful", url);
            return Result.healthy();
        }

        log.debug("Health check against url={} failed with response={}", url, httpHealthResponse);
        return Result.unhealthy("Http health check against url=%s failed with response=%s", url, httpHealthResponse);
    }

    /**
     * Performs a health check via HTTP against an external dependency.
     * By default uses the Jersey 2 HTTP client, but can be overridden to allow for different behavior.
     * @param url the URL to check.
     * @return response from the health check.
     */
    protected HttpHealthResponse httpCheck(final String url) {
        final Response response = client.target(url).request().get();

        final String entityString = response.readEntity(String.class);

        return new HttpHealthResponse(response.getStatus(), entityString);
    }

    /**
     * Validates the response from the health check.
     * By default checks if the response status is 2xx.
     * @param httpHealthResponse The response resulting from the http health check.
     * @return healthiness flag.
     */
    protected boolean isHealthResponseValid(final HttpHealthResponse httpHealthResponse) {
        final Response.Status.Family statusFamily = Response.Status.Family.familyOf(httpHealthResponse.getStatus());

        return statusFamily == Response.Status.Family.SUCCESSFUL;
    }
}
