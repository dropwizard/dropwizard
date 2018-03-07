package io.dropwizard.logging.socket;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardUdpSocketAppenderTest {

    private OutputStreamAppender<ILoggingEvent> udpStreamAppender;

    private DatagramSocket datagramSocket;
    private Thread thread;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    @Before
    public void setUp() throws Exception {
        datagramSocket = new DatagramSocket();
        thread = new Thread(() -> {
            byte[] buffer = new byte[128];
            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                try {
                    datagramSocket.receive(datagramPacket);
                    assertThat(new String(buffer, 0, datagramPacket.getLength(), UTF_8))
                        .isEqualTo("Test message");
                    countDownLatch.countDown();
                } catch (IOException e) {
                    break;
                }
            }
        });
        thread.start();
        udpStreamAppender = new DropwizardUdpSocketAppender<>("localhost", datagramSocket.getLocalPort());
        udpStreamAppender.setContext(Mockito.mock(Context.class));
        udpStreamAppender.start();
    }

    @After
    public void tearDown() throws Exception {
        datagramSocket.close();
        thread.interrupt();
        udpStreamAppender.stop();
    }

    @Test
    public void testSendMessage() throws Exception {
        udpStreamAppender.getOutputStream().write("Test message".getBytes(UTF_8));

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
    }
}
