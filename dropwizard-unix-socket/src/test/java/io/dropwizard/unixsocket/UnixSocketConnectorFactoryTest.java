package io.dropwizard.unixsocket;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.*;
import java.net.StandardProtocolFamily;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.SocketChannel;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ExtendWith(DropwizardExtensionsSupport.class)
public class UnixSocketConnectorFactoryTest {
    private static final String httpRequest = "GET /app/hello HTTP/1.1\r\n" +
        "Host: dropwizard-unixsock\r\n" +
        "\r\n";

    public DropwizardAppExtension<Configuration> ext = new DropwizardAppExtension<>(HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("yaml/usock-server.yml"));

    private UnixDomainSocketAddress socketAddress;

    @BeforeEach
    public void setUp() {
        final File socket = new File("/tmp/dropwizard.sock");
        socket.deleteOnExit();

        socketAddress = UnixDomainSocketAddress.of(socket.getPath());
    }

    @Test
    public void testClient() throws Exception {
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(socketAddress);
            OutputStream os = Channels.newOutputStream(channel);
            try (OutputStreamWriter osw = new OutputStreamWriter(os, UTF_8)) {
                try (PrintWriter writer = new PrintWriter(osw)) {
                    InputStream is = Channels.newInputStream(channel);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
                        writer.print(httpRequest);
                        writer.flush();

                        verifyHelloWorldReceived(reader);
                    }
                }
            }
        }
    }

    @Test
    public void testManyCalls() throws Exception {
        try (SocketChannel channel = SocketChannel.open(StandardProtocolFamily.UNIX)) {
            channel.connect(socketAddress);
            OutputStream os = Channels.newOutputStream(channel);
            try (OutputStreamWriter osw = new OutputStreamWriter(os, UTF_8)) {
                try (PrintWriter writer = new PrintWriter(osw)) {
                    InputStream is = Channels.newInputStream(channel);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, UTF_8))) {
                        for (int i = 0; i < 1000; i++) {
                            writer.print(httpRequest);
                            writer.flush();

                            verifyHelloWorldReceived(reader);
                        }
                    }
                }
            }
        }
    }

    private static void verifyHelloWorldReceived(BufferedReader reader) throws IOException {
        for (int i = 0; i < 5; i++) {
            String line = reader.readLine();
            if (line.isEmpty()) {
                // here comes body. Need to be specific otherwise it hangs for a long time.
                char[] buffer = new char[18];
                int read = reader.read(buffer, 0, 18);
                assertThat(read).isEqualTo(18);
                assertThat(new String(buffer)).isEqualTo("{\"hello\": \"World\"}");
                return;
            }
        }
        fail("Have not received hello world.");
    }

    public static class HelloWorldApplication extends Application<Configuration> {

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(new FakeResource());
            environment.healthChecks().register("hello-check", new HealthCheck() {
                @Override
                protected Result check() {
                    return Result.healthy();
                }
            });
        }

        @Path("/hello")
        @Produces(MediaType.APPLICATION_JSON)
        public static class FakeResource {

            @GET
            public String get() {
                return "{\"hello\": \"World\"}";
            }
        }
    }
}
