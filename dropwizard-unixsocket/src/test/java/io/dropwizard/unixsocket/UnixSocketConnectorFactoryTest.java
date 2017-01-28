package io.dropwizard.unixsocket;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import jnr.unixsocket.UnixSocketAddress;
import jnr.unixsocket.UnixSocketChannel;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.nio.channels.Channels;

public class UnixSocketConnectorFactoryTest {

    @Rule
    public DropwizardAppRule<Configuration> rule = new DropwizardAppRule<>(HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("yaml/usock-server.yml"));

    private File socket = new File("/tmp/dropwizard.sock");

    private String httpRequest = "GET /app/hello HTTP/1.1\r\n" +
        "Host: dropwizard-unixsock\r\n" +
        "\r\n";

    @Test
    public void testClient() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(new UnixSocketAddress(socket));
             PrintWriter writer = new PrintWriter(Channels.newOutputStream(channel));
             Reader reader = new InputStreamReader(Channels.newInputStream(channel))) {
            writer.print(httpRequest);
            writer.flush();

            char[] buf = new char[4096];
            int read = reader.read(buf);
            System.out.println("Read from the server: " + new String(buf, 0, read));
        }
    }

    @Test
    public void testManyClientCalls() throws Exception {
        try (UnixSocketChannel channel = UnixSocketChannel.open(new UnixSocketAddress(socket));
             PrintWriter writer = new PrintWriter(Channels.newOutputStream(channel));
             Reader reader = new InputStreamReader(Channels.newInputStream(channel))) {
            char[] buf = new char[4096];
            for (int i = 0; i < 1000; i++) {
                writer.print(httpRequest);
                writer.flush();

                int read = reader.read(buf);
                System.out.println("Read from the server: " + new String(buf, 0, read));
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
