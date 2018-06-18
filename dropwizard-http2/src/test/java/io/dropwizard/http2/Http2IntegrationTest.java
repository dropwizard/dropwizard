package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2IntegrationTest extends AbstractHttp2Test {

    @Rule
    public final DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2.yml"),
            Optional.of("tls_http2"),
            ConfigOverride.config("tls_http2", "server.connector.keyStorePath",
                    ResourceHelpers.resourceFilePath("stores/http2_server.jks")),
            ConfigOverride.config("tls_http2", "server.connector.trustStorePath",
                    ResourceHelpers.resourceFilePath("stores/http2_client.jts"))
    );

    @Test
    public void testHttp11() throws Exception {
        final String hostname = "localhost";
        final int port = appRule.getLocalPort();
        final JerseyClient http11Client = new JerseyClientBuilder()
                .sslContext(sslContextFactory.getSslContext())
                .build();
        final Response response = http11Client.target("https://" + hostname + ":" + port + "/api/test")
                .request()
                .get();
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.readEntity(String.class)).isEqualTo(FakeApplication.HELLO_WORLD);
        http11Client.close();
    }

    @Test
    public void testHttp2() throws Exception {
        assertResponse(client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"));
    }

    @Test
    public void testHttp2ManyRequests() throws Exception {
        performManyAsyncRequests(client, "https://localhost:" + appRule.getLocalPort() + "/api/test");
    }
}
