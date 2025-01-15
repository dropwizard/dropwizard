package io.dropwizard.unixsocket;

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
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.io.ClientConnector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@DisabledOnOs(OS.WINDOWS) // Windows has Unix Socket support but not on all versions.
@ExtendWith(DropwizardExtensionsSupport.class)
class UnixSocketConnectorFactoryTest {

    public DropwizardAppExtension<Configuration> ext = new DropwizardAppExtension<>(HelloWorldApplication.class,
        ResourceHelpers.resourceFilePath("yaml/usock-server.yml"));

    private HttpClient httpClient;

    @BeforeEach
    void setUp() throws Exception {
        ClientConnector clientConnector = ClientConnector.forUnixDomain(Paths.get("/tmp", "dropwizard.sock"));
        httpClient = new HttpClient(new HttpClientTransportOverHTTP(clientConnector));
        httpClient.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        httpClient.stop();
    }

    @Test
    void testClient() throws Exception {
        assertThat(httpClient.GET("http://localhost:0/app/hello"))
            .extracting(ContentResponse::getContentAsString)
            .isEqualTo("{\"hello\": \"World\"}");
    }

    @Test
    void testManyCalls() throws Exception {
        for (int i = 0; i < 1000; i++) {
            testClient();
        }
    }

    public static class HelloWorldApplication extends Application<Configuration> {

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(new FakeResource());
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
