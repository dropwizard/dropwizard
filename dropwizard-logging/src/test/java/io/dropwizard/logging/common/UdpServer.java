package io.dropwizard.logging.common;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

public class UdpServer implements AutoCloseable {

    private final DatagramSocket socket;
    private final ExecutorService es;
    private int messageCount;

    public UdpServer(DatagramSocket socket, int messageCount) {
        this.messageCount = messageCount;
        this.socket = socket;
        es = Executors.newFixedThreadPool(1);
    }

    public Future<List<String>> receive() {
        return es.submit(() -> {
            List<String> messages = new ArrayList<>();
            for (; messageCount > 0; messageCount--){
                byte[] buffer = new byte[128];
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    socket.receive(datagramPacket);
                    messages.add(new String(buffer, 0, datagramPacket.getLength(), UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException("Error reading logs", e);
                }
            }
            return messages;
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
