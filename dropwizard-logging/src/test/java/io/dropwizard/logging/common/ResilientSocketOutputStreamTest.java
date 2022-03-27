package io.dropwizard.logging.common;

import org.junit.jupiter.api.Test;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

class ResilientSocketOutputStreamTest {
    @Test
    void testCreatesCleanOutputStream() throws IOException {
        try (ServerSocket ss = new ServerSocket(0); ResilientSocketOutputStream resilientSocketOutputStream = new ResilientSocketOutputStream("localhost", ss.getLocalPort(),
            1024, 500, SocketFactory.getDefault())) {
                assertThat(resilientSocketOutputStream.presumedClean).isTrue();
                assertThat(resilientSocketOutputStream.os).isNotNull();
            }
    }

    @Test
    void testThrowsExceptionIfCantCreateOutputStream() {
        assertThatIllegalStateException().isThrownBy(() -> new ResilientSocketOutputStream("256.256.256.256", 1024,
            100, 1024, SocketFactory.getDefault()))
            .withMessage("Unable to create a TCP connection to 256.256.256.256:1024");
    }

    @Test
    void testWriteMessage() throws Exception {
        try (ServerSocket ss = new ServerSocket(0);
             TcpServer tcpServer = new TcpServer(ss);
             ResilientSocketOutputStream resilientSocketOutputStream = new ResilientSocketOutputStream("localhost", ss.getLocalPort(), 1024, 500, SocketFactory.getDefault())) {
            Future<List<String>> receivedMessages = tcpServer.receive();
            resilientSocketOutputStream.write("Test message".getBytes(StandardCharsets.UTF_8));
            resilientSocketOutputStream.close();

            assertThat(receivedMessages.get(5, TimeUnit.SECONDS))
                .singleElement()
                .isEqualTo("Test message");
        }
    }

    @Test
    void testGetDescription() throws IOException {
        try (ServerSocket ss = new ServerSocket(0); ResilientSocketOutputStream resilientSocketOutputStream = new ResilientSocketOutputStream("localhost", ss.getLocalPort(),
            1024, 500, SocketFactory.getDefault())) {
            assertThat(resilientSocketOutputStream.getDescription())
                .isEqualTo("tcp [localhost:%d]", ss.getLocalPort());
        }
    }
}
