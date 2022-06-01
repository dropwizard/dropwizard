package io.dropwizard.logging.common.socket;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.OutputStreamAppender;
import io.dropwizard.logging.common.UdpServer;
import java.net.DatagramSocket;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DropwizardUdpSocketAppenderTest {

    @Test
    void testSendMessage() throws Exception {
        try (DatagramSocket datagramSocket = new DatagramSocket();
                UdpServer udpServer = new UdpServer(datagramSocket, 1)) {
            Future<List<String>> receivedMessage = udpServer.receive();
            OutputStreamAppender<ILoggingEvent> udpStreamAppender =
                    new DropwizardUdpSocketAppender<>("localhost", datagramSocket.getLocalPort());
            udpStreamAppender.setContext(Mockito.mock(Context.class));
            udpStreamAppender.start();
            udpStreamAppender.getOutputStream().write("Test message".getBytes(UTF_8));

            assertThat(receivedMessage.get(5, TimeUnit.SECONDS)).singleElement().isEqualTo("Test message");
            udpStreamAppender.stop();
        }
    }
}
