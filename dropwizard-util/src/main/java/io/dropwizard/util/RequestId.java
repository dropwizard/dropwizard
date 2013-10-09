package io.dropwizard.util;

/**
 * Common keys for fetching/storing request ids in the underlying logger MDC.
 *
 * @see RequestIdLoggingFilter
 */
public class RequestId {
    public static final String SERVICE_REQUEST_ID = "Service-Request-Id";
    public static final String CLIENT_REQUEST_ID = "Client-Request-Id";
}
