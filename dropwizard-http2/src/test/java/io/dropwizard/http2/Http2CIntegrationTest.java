package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.HTTP2ClientConnectionFactory;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class Http2CIntegrationTest  extends AbstractHttp2Test {

    public DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2c.yml"));

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.setClientConnectionFactory(new HTTP2ClientConnectionFactory()); // No need for ALPN
        client = new HttpClient(new HttpClientTransportOverHTTP2(http2Client), null);
        client.start();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        client.stop();
    }

    @Test
    public void testHttp11() {
        final String hostname = "127.0.0.1";
        final int port = appRule.getLocalPort();
        final JerseyClient http11Client = new JerseyClientBuilder().build();
        final Response response = http11Client.target("http://" + hostname + ":" + port + "/api/test")
                .request()
                .get();
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.readEntity(String.class)).isEqualTo(FakeApplication.HELLO_WORLD);
        http11Client.close();
    }

    @Test
    public void testHttp2c() throws Exception {
        assertResponse(client.GET("http://localhost:" + appRule.getLocalPort() + "/api/test"));
    }

    @Test
    public void testHttp2cManyRequests() throws Exception {
        performManyAsyncRequests(client, "http://localhost:" + appRule.getLocalPort() + "/api/test");
    }
}
