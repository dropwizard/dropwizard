package io.dropwizard.unixsocket;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.CharStreams;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;

public class UnixSocketConnectorFactoryTest {
    private static final String httpRequest = "GET /app/hello HTTP/1.1\r\n" +
        "Host: dropwizard-unixsock\r\n" +
        "\r\n";

    @Rule
    public DropwizardAppRule<Configuration> rule = new DropwizardAppRule<>(HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("yaml/usock-server.yml"));

    private UnixSocketAddress socketAddress;

    @BeforeClass
    public static void initialize() {
        assumeFalse(SystemUtils.IS_OS_WINDOWS);
    }

    @Before
    public void setUp() {
        final File socket = new File("/tmp/dropwizard.sock");
        socket.deleteOnExit();

        socketAddress = new UnixSocketAddress(socket);
    }

    @Test
    public void testClient() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(socketAddress);
             OutputStream os = Channels.newOutputStream(channel);
             OutputStreamWriter osw = new OutputStreamWriter(os, UTF_8);
             PrintWriter writer = new PrintWriter(osw);
             InputStream is = Channels.newInputStream(channel);
             InputStreamReader reader = new InputStreamReader(is, UTF_8)) {
            writer.print(httpRequest);
            writer.flush();

            String response = CharStreams.toString(reader);
            assertThat(response)
                    .isEqualToNormalizingNewlines("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: application/json\r\n" +
                            "Vary: Accept-Encoding\r\n" +
                            "Content-Length: 18\r\n" +
                            "\r\n" +
                            "{\"hello\": \"World\"}");
        }
    }

    @Test
    public void testManyCalls() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(socketAddress);
             OutputStream os = Channels.newOutputStream(channel);
             OutputStreamWriter osw = new OutputStreamWriter(os, UTF_8);
             PrintWriter writer = new PrintWriter(osw);
             InputStream is = Channels.newInputStream(channel);
             InputStreamReader reader = new InputStreamReader(is, UTF_8)) {
            for (int i = 0; i < 1000; i++) {
                writer.print(httpRequest);
                writer.flush();

                String response = CharStreams.toString(reader);
                assertThat(response)
                        .describedAs("HTTP body in iteration %d", i)
                        .isEqualToNormalizingNewlines("HTTP/1.1 200 OK\r\n" +
                                "Content-Type: application/json\r\n" +
                                "Vary: Accept-Encoding\r\n" +
                                "Content-Length: 18\r\n" +
                                "\r\n" +
                                "{\"hello\": \"World\"}");
            }
        }
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
