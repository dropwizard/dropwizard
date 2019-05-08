package ch.qos.logback.core.recovery;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

public class ResilientSocketOutputStreamTest {

    private ResilientSocketOutputStream resilientSocketOutputStream;

    private Thread thread;
    private ServerSocket ss;
    private CountDownLatch latch = new CountDownLatch(1);

    @BeforeEach
    public void setUp() throws Exception {
        ss = new ServerSocket(0);
        thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket socket = ss.accept()) {
                    byte[] buffer = new byte[128];
                    int read = socket.getInputStream().read(buffer);
                    if (read > 0) {
                        assertThat(new String(buffer, 0, read, StandardCharsets.UTF_8)).isEqualTo("Test message");
                        latch.countDown();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        });
        thread.start();
        resilientSocketOutputStream = new ResilientSocketOutputStream("localhost", ss.getLocalPort(),
            1024, 500, SocketFactory.getDefault());
    }

    @AfterEach
    public void tearDown() throws Exception {
        ss.close();
        thread.interrupt();
        resilientSocketOutputStream.close();
    }

    @Test
    public void testCreatesCleanOutputStream() throws Exception {
        assertThat(resilientSocketOutputStream.presumedClean).isTrue();
        assertThat(resilientSocketOutputStream.os).isNotNull();
    }

    @Test
    public void testThrowsExceptionIfCantCreateOutputStream() throws Exception {
        assertThatIllegalStateException().isThrownBy(() -> new ResilientSocketOutputStream("256.256.256.256", 1024,
            100, 1024, SocketFactory.getDefault()))
            .withMessage("Unable to create a TCP connection to 256.256.256.256:1024");
    }

    @Test
    public void testWriteMessage() throws Exception {
        resilientSocketOutputStream.write("Test message".getBytes(StandardCharsets.UTF_8));
        resilientSocketOutputStream.flush();

        latch.await(5, TimeUnit.SECONDS);
        assertThat(latch.getCount()).isEqualTo(0);
    }

    @Test
    public void testGetDescription() {
        assertThat(resilientSocketOutputStream.getDescription()).isEqualTo(String.format("tcp [localhost:%d]",
            ss.getLocalPort()));
    }
}
