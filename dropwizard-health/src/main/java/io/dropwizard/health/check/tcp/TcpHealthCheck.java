package io.dropwizard.health.check.tcp;

import com.codahale.metrics.health.HealthCheck;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Objects;

public class TcpHealthCheck extends HealthCheck {
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpHealthCheck.class);

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(2);

    @NonNull
    private final String host;
    private final int port;
    private final Duration connectionTimeout;

    public TcpHealthCheck(@NonNull final String host,
                          final int port) {
        this(host, port, DEFAULT_CONNECTION_TIMEOUT);
    }

    public TcpHealthCheck(@NonNull final String host,
                          final int port,
                          final Duration connectionTimeout) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        if (connectionTimeout.isNegative()) {
            throw new IllegalStateException("connectionTimeout must be a non-negative value.");
        }
        if (connectionTimeout.toMillis() > Integer.MAX_VALUE) {
            throw new IllegalStateException("Cannot configure a connectionTimeout greater than the max integer value");
        }
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    protected Result check() throws IOException {
        final boolean isHealthy = tcpCheck(host, port);

        if (isHealthy) {
            LOGGER.debug("Health check against url={}:{} successful", host, port);
            return Result.healthy();
        }

        LOGGER.debug("Health check against url={}:{} failed", host, port);
        return Result.unhealthy("TCP health check against host=%s port=%s failed", host, port);
    }

    /**
     * Performs a health check via TCP against an external dependency.
     * By default, uses the Java {@link Socket} API, but can be overridden to allow for different behavior.
     *
     * @param host the host to check.
     * @param port the port to check.
     * @return whether the check was successful or not.
     */
    protected boolean tcpCheck(final String host, final int port) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), (int) connectionTimeout.toMillis());
            return socket.isConnected();
        }
    }

    // visible for testing
    Duration getConnectionTimeout() {
        return connectionTimeout;
    }
}
