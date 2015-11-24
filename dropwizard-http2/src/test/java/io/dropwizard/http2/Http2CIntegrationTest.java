package io.dropwizard.http2;

import com.google.common.base.Charsets;
import com.google.common.net.HttpHeaders;
import io.dropwizard.Configuration;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.HTTP2ClientConnectionFactory;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class Http2CIntegrationTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Rule
    public DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2c.yml"));

    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.setClientConnectionFactory(new HTTP2ClientConnectionFactory()); // No need for ALPN
        client = new HttpClient(new HttpClientTransportOverHTTP2(http2Client), null);
        client.start();
    }

    @After
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
        final String hostname = "localhost";
        final int port = appRule.getLocalPort();

        final ContentResponse response = client.GET("http://" + hostname + ":" + port + "/api/test");
        assertThat(response.getVersion()).isEqualTo(HttpVersion.HTTP_2);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(FakeApplication.HELLO_WORLD);
    }

    @Test
    public void testHttp2cManyRequests() throws Exception {
        final String hostname = "localhost";
        final int port = appRule.getLocalPort();

        // For some reason the library requires to perform the first request synchronously with HTTP/2
        testHttp2c();

        final int amount = 100;
        final CountDownLatch latch = new CountDownLatch(amount);
        for (int i = 0; i < amount; i++) {
            client.newRequest("http://" + hostname + ":" + port + "/api/test")
                    .send(new BufferingResponseListener() {
                        @Override
                        public void onComplete(Result result) {
                            assertThat(result.getResponse().getVersion()).isEqualTo(HttpVersion.HTTP_2);
                            assertThat(result.getResponse().getStatus()).isEqualTo(200);
                            assertThat(getContentAsString(Charsets.UTF_8)).isEqualTo(FakeApplication.HELLO_WORLD);
                            latch.countDown();
                        }
                    });
        }

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
