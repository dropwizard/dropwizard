package io.dropwizard.health.check.http;

import com.sun.net.httpserver.HttpServer;
import jakarta.ws.rs.ProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class HttpHealthCheckTest {
    private static final String SUCCESS_PATH = "/ping";
    private static final String FAIL_PATH = "/fail";
    private static final String TIMEOUT_PATH = "/timeout";
    private static final String BASE_URI = "http://127.0.0.1:";

    private HttpServer httpServer;

    @BeforeEach
    void setup() throws Exception {
        httpServer = HttpServer.create(new InetSocketAddress(0), 0);
    }

    @AfterEach
    void tearDown() throws Exception {
        httpServer.stop(0);
    }

    @Test
    void httpHealthCheckShouldConsiderA200ResponseHealthy() {
        httpServer.createContext(SUCCESS_PATH, httpExchange -> {
            try {
                httpExchange.sendResponseHeaders(200, 0);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();
        final HttpHealthCheck httpHealthCheck = new HttpHealthCheck(BASE_URI +
            httpServer.getAddress().getPort() + SUCCESS_PATH);
        assertThat(httpHealthCheck.check().isHealthy()).isTrue();
    }

    @Test
    void httpHealthCheckShouldConsiderA500ResponseUnhealthy() {
        httpServer.createContext(FAIL_PATH, httpExchange -> {
            try {
                httpExchange.sendResponseHeaders(500, 0);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();
        final HttpHealthCheck httpHealthCheck = new HttpHealthCheck(BASE_URI +
            httpServer.getAddress().getPort() + FAIL_PATH);
        assertThat(httpHealthCheck.check().isHealthy()).isFalse();
    }

    @Test
    void httpHealthCheckShouldConsiderATimeoutUnhealthy() {
        httpServer.createContext(TIMEOUT_PATH, httpExchange -> {
            try {
                Thread.sleep(HttpHealthCheck.DEFAULT_TIMEOUT.toMillis() * 2);
                httpExchange.sendResponseHeaders(200, 0);
            } catch (InterruptedException e) {
                httpExchange.sendResponseHeaders(500, 0);
            } finally {
                httpExchange.close();
            }
        });
        httpServer.start();
        final HttpHealthCheck httpHealthCheck = new HttpHealthCheck(BASE_URI +
            httpServer.getAddress().getPort() + TIMEOUT_PATH);

        assertThatExceptionOfType(ProcessingException.class)
            .isThrownBy(httpHealthCheck::check);
    }
}
