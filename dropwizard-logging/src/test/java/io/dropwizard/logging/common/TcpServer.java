package io.dropwizard.logging.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

class TcpServer implements AutoCloseable {

    private final ServerSocket serverSocket;
    private final ExecutorService es;

    TcpServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        es = Executors.newFixedThreadPool(1);
    }

    Future<List<String>> receive() {
        return es.submit(() -> {
            try (Socket s = serverSocket.accept(); BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.toList());
            } catch (IOException e) {
                throw new IllegalStateException("Error reading logs", e);
            }
        });
    }

    @Override
    public void close() {
        es.shutdownNow();
        try {
            if (!es.awaitTermination(1, TimeUnit.MINUTES)) {
                throw new IllegalStateException("server did not terminate");
            }
        } catch (InterruptedException ie) {
            es.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
