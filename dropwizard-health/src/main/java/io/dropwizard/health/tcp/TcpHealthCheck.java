package io.dropwizard.health.tcp;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Duration;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpHealthCheck extends HealthCheck {
    private static final Logger log = LoggerFactory.getLogger(TcpHealthCheck.class);

    private static final Duration DEFAULT_CONNECTION_TIMEOUT = Duration.ofSeconds(2);

    @Nonnull
    private final String host;
    private final int port;
    private final Duration connectionTimeout;

    public TcpHealthCheck(@Nonnull final String host,
                          final int port) {
        this(host, port, DEFAULT_CONNECTION_TIMEOUT);
    }

    public TcpHealthCheck(@Nonnull final String host,
                          final int port,
                          final Duration connectionTimeout) {
        this.host = Objects.requireNonNull(host);
        this.port = port;
        Preconditions.checkState(!connectionTimeout.isNegative(), "connectionTimeout must be a non-negative value.");
        Preconditions.checkState(connectionTimeout.toMillis() <= Integer.MAX_VALUE,
                "Cannot configure a connectionTimeout greater than the max integer value");
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    protected Result check() throws IOException {
        final boolean isHealthy = tcpCheck(host, port);

        if (isHealthy) {
            log.debug("Health check against url={}:{} successful", host, port);
            return Result.healthy();
        }

        log.debug("Health check against url={}:{} failed", host, port);
        return Result.unhealthy("TCP health check against host=%s port=%s failed", host, port);
    }

    /**
     * Performs a health check via TCP against an external dependency.
     * By default uses the Java {@link Socket} API, but can be overridden to allow for different behavior.
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
