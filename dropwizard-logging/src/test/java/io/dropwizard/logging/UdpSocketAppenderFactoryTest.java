package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class UdpSocketAppenderFactoryTest {

    private static final int UDP_PORT = 32144;

    private Thread thread;
    private DatagramSocket datagramSocket;

    private int messagesCount = 100;
    private CountDownLatch countDownLatch = new CountDownLatch(messagesCount);

    @Before
    public void setUp() throws Exception {
        datagramSocket = new DatagramSocket(UDP_PORT);
        thread = new Thread(() -> {
            byte[] buffer = new byte[256];
            for (int i = 0; i < messagesCount; i++) {
                try {
                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);
                    datagramSocket.receive(datagramPacket);
                    assertThat(new String(buffer, 0, datagramPacket.getLength(), StandardCharsets.UTF_8))
                        .startsWith("INFO").contains("com.example.app: Application log " + i);
                    countDownLatch.countDown();
                } catch (SocketException e) {
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @After
    public void tearDown() throws Exception {
        thread.interrupt();
        datagramSocket.close();
    }

    @Test
    public void testSendLogsByTcp() throws Exception {
        ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(UdpSocketAppenderFactory.class);

        DefaultLoggingFactory loggingFactory = new YamlConfigurationFactory<>(DefaultLoggingFactory.class,
            BaseValidator.newValidator(), objectMapper, "dw-udp")
            .build(new File(Resources.getResource("yaml/logging-udp.yml").toURI()));
        loggingFactory.configure(new MetricRegistry(), "udp-test");

        Logger logger = LoggerFactory.getLogger("com.example.app");
        for (int i = 0; i < 100; i++) {
            logger.info("Application log {}", i);
        }

        countDownLatch.await(5, TimeUnit.SECONDS);
        assertThat(countDownLatch.getCount()).isEqualTo(0);
        loggingFactory.reset();
    }
}
