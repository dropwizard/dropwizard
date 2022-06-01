package io.dropwizard.logging.common.socket;

import ch.qos.logback.core.OutputStreamAppender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Sends log events to a UDP server, a connection to which is represented as a stream.
 */
public class DropwizardUdpSocketAppender<E extends DeferredProcessingAware> extends OutputStreamAppender<E> {

    private final String host;
    private final int port;

    public DropwizardUdpSocketAppender(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {
        setOutputStream(datagramSocketOutputStream(host, port));
        super.start();
    }

    protected OutputStream datagramSocketOutputStream(String host, int port) {
        try {
            return new OutputStream() {
                private final DatagramSocket datagramSocket = new DatagramSocket();

                @Override
                public void write(int b) throws IOException {
                    throw new UnsupportedOperationException("Datagram doesn't work at byte level");
                }

                @Override
                public void write(byte[] b, int off, int len) throws IOException {
                    // Important not to cache InetAddress and let the JVM/OS to handle DNS caching.
                    datagramSocket.send(new DatagramPacket(b, off, len, InetAddress.getByName(host), port));
                }

                @Override
                public void close() throws IOException {
                    datagramSocket.close();
                }
            };
        } catch (SocketException e) {
            throw new IllegalStateException("Unable to create a datagram socket", e);
        }
    }
}
