package io.dropwizard.health.check.http;

import com.codahale.metrics.health.HealthCheck;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.Objects;

public class HttpHealthCheck extends HealthCheck {
    // visible for testing
    static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);
    @SuppressWarnings("Slf4jLoggerShouldBeNonStatic")
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHealthCheck.class);
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
        if (readTimeout.toMillis() <= 0L || connectionTimeout.toMillis() <= 0L) {
            throw new IllegalStateException();
        }
        this.client = JerseyClientBuilder.createClient()
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
            LOGGER.debug("Health check against url={} successful", url);
            return Result.healthy();
        }

        LOGGER.debug("Health check against url={} failed with response={}", url, httpHealthResponse);
        return Result.unhealthy("Http health check against url=%s failed with response=%s", url, httpHealthResponse);
    }

    /**
     * Performs a health check via HTTP against an external dependency.
     * By default uses the Jersey 2 HTTP client, but can be overridden to allow for different behavior.
     *
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
     *
     * @param httpHealthResponse The response resulting from the http health check.
     * @return healthiness flag.
     */
    protected boolean isHealthResponseValid(final HttpHealthResponse httpHealthResponse) {
        final Response.Status.Family statusFamily = Response.Status.Family.familyOf(httpHealthResponse.getStatus());

        return statusFamily == Response.Status.Family.SUCCESSFUL;
    }
}
