package io.dropwizard.documentation.mq;

import java.io.Closeable;

/**
 * Some message queue client
 */
public final class MessageQueueClient implements Closeable {
    private final String host;
    private final int port;

    public MessageQueueClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void close() {
        // NOP
    }
}
