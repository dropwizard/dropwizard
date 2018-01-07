package io.dropwizard.logging.socket;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.recovery.ResilentSocketOutputStream;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import javax.net.SocketFactory;
import java.io.OutputStream;

/**
 * Sends log events to a TCP server, a connection to which is represented as {@link ResilentSocketOutputStream}.
 */
public class DropwizardSocketAppender<E extends DeferredProcessingAware> extends OutputStreamAppender<E> {

    private final String host;
    private final int port;
    private final int connectionTimeoutMs;
    private final int sendBufferSize;
    private final SocketFactory socketFactory;

    public DropwizardSocketAppender(String host, int port, int connectionTimeoutMs, int sendBufferSize,
                                    SocketFactory socketFactory) {
        this.host = host;
        this.port = port;
        this.connectionTimeoutMs = connectionTimeoutMs;
        this.sendBufferSize = sendBufferSize;
        this.socketFactory = socketFactory;
    }

    @Override
    public void start() {
        setOutputStream(socketOutputStream());
        super.start();
    }

    protected OutputStream socketOutputStream() {
        final ResilentSocketOutputStream outputStream = new ResilentSocketOutputStream(host, port,
            connectionTimeoutMs, sendBufferSize, socketFactory);
        outputStream.setContext(context);
        return outputStream;
    }
}

