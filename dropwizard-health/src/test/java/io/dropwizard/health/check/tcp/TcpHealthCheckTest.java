package io.dropwizard.health.check.tcp;

import com.google.common.util.concurrent.Uninterruptibles;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TcpHealthCheckTest {
    private ServerSocket serverSocket;
    private TcpHealthCheck tcpHealthCheck;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0);
        tcpHealthCheck = new TcpHealthCheck("127.0.0.1", serverSocket.getLocalPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        serverSocket.close();
    }

    @Test
    void tcpHealthCheckShouldReturnHealthyIfCanConnect() throws IOException {
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> serverSocket.accept());
        assertThat(tcpHealthCheck.check().isHealthy())
            .isTrue();
    }

    @Test
    void tcpHealthCheckShouldReturnUnhealthyIfCannotConnect() throws IOException {
        serverSocket.close();
        assertThrows(ConnectException.class, () -> tcpHealthCheck.check());
    }

    @Test
    void tcpHealthCheckShouldReturnUnhealthyIfCannotConnectWithinConfiguredTimeout() throws IOException {
        final int port = serverSocket.getLocalPort();
        serverSocket.setReuseAddress(true);
        serverSocket.close();

        serverSocket = new ServerSocket();
        final ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(() -> {
            Uninterruptibles.sleepUninterruptibly(tcpHealthCheck.getConnectionTimeout().toMillis() * 3, TimeUnit.MILLISECONDS);
            serverSocket.bind(new InetSocketAddress("127.0.0.1", port));
            return true;
        });

        assertThrows(ConnectException.class, () -> tcpHealthCheck.check());
    }
}
