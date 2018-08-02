package io.dropwizard.unixsocket;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.channels.Channels;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assume.assumeFalse;

public class UnixSocketConnectorFactoryTest {

    @Rule
    public DropwizardAppRule<Configuration> rule = new DropwizardAppRule<>(HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("yaml/usock-server.yml"));

    private File socket = new File("/tmp/dropwizard.sock");

    private String httpRequest = "GET /app/hello HTTP/1.1\r\n" +
        "Host: dropwizard-unixsock\r\n" +
        "\r\n";

    @BeforeClass
    public static void setUp() {
        assumeFalse(SystemUtils.IS_OS_WINDOWS);
    }

    @Test
    public void testClient() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(new UnixSocketAddress(socket));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(Channels.newOutputStream(channel), UTF_8));
             Reader reader = new InputStreamReader(Channels.newInputStream(channel), UTF_8)) {
            writer.print(httpRequest);
            writer.flush();

            char[] buf = new char[256];
            int readHeaders = reader.read(buf);
            assertThat(readHeaders).isGreaterThan(0);
            String headers = new String(buf, 0, readHeaders);
            int readBody = reader.read(buf);
            assertThat(readBody).isGreaterThan(0);
            String body = new String(buf, 0, readBody);
            assertThat(headers).isEqualToNormalizingNewlines("HTTP/1.1 200 OK\r\n" +
                                                             "Content-Type: application/json\r\n" +
                                                             "Vary: Accept-Encoding\r\n" +
                                                             "Content-Length: 18\r\n" +
                                                             "\r\n");
            assertThat(body).isEqualTo("{\"hello\": \"World\"}");
        }
    }

    @Test
    public void testManyCalls() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(new UnixSocketAddress(socket));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(Channels.newOutputStream(channel), UTF_8));
             Reader reader = new InputStreamReader(Channels.newInputStream(channel), UTF_8)) {
            char[] buf = new char[256];
            for (int i = 0; i < 1000; i++) {
                writer.print(httpRequest);
                writer.flush();

                int readHeaders = reader.read(buf);
                assertThat(readHeaders).isGreaterThan(0);
                String headers = new String(buf, 0, readHeaders);
                int readBody = reader.read(buf, 0, 18);
                assertThat(readBody).isGreaterThan(0);
                String body = new String(buf, 0, readBody);
                assertThat(headers).isEqualToNormalizingNewlines("HTTP/1.1 200 OK\r\n" +
                                                                 "Content-Type: application/json\r\n" +
                                                                 "Vary: Accept-Encoding\r\n" +
                                                                 "Content-Length: 18\r\n" +
                                                                 "\r\n");
                assertThat(body).isEqualTo("{\"hello\": \"World\"}");
            }
        }
    }

    public static class HelloWorldApplication extends Application<Configuration> {

        @Override
        public void run(Configuration configuration, Environment environment) throws Exception {
            environment.jersey().register(new FakeResource());
            environment.healthChecks().register("hello-check", new HealthCheck() {
                @Override
                protected Result check() throws Exception {
                    return Result.healthy();
                }
            });
        }

        @Path("/hello")
        @Produces(MediaType.APPLICATION_JSON)
        public static class FakeResource {

            @GET
            public String get() throws Exception {
                return "{\"hello\": \"World\"}";
            }
        }
    }
}
