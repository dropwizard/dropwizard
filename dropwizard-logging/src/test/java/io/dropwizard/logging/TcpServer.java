package io.dropwizard.logging;

import org.junit.rules.ExternalResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class TcpServer extends ExternalResource {

    private final Thread thread;
    private final ServerSocket serverSocket;
    private final int messageCount = 100;
    private final CountDownLatch latch = new CountDownLatch(messageCount);

    TcpServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                } catch (SocketException e) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                new Thread(() -> readAndVerifyData(socket)).start();
            }
        });
    }

    ServerSocket getServerSocket() {
        return serverSocket;
    }

    int getMessageCount() {
        return messageCount;
    }

    CountDownLatch getLatch() {
        return latch;
    }

    int getPort() {
        return serverSocket.getLocalPort();
    }

    @Override
    protected void before() throws Throwable {
        thread.start();
    }

    @Override
    protected void after() {
        thread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void readAndVerifyData(Socket socket) {
        try (Socket s = socket; BufferedReader reader = new BufferedReader(new InputStreamReader(
            s.getInputStream(), StandardCharsets.UTF_8))) {
            for (int i = 0; i < messageCount; i++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                assertThat(line).startsWith("INFO").contains("com.example.app: Application log " + i);
                latch.countDown();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
