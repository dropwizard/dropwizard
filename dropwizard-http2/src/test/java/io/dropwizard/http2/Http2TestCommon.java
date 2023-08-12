package io.dropwizard.http2;

import io.dropwizard.logging.common.BootstrapLogging;
import org.eclipse.jetty.client.BufferingResponseListener;
import org.eclipse.jetty.client.ContentResponse;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.Result;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.util.resource.PathResourceFactory;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common code for HTTP/2 connector tests
 */
class Http2TestCommon {

    static {
        BootstrapLogging.bootstrap();
    }

    final SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
    HttpClient http2Client;
    HttpClient http1Client;

    @BeforeEach
    void setUp() throws Exception {
        ResourceFactory resourceFactory = new PathResourceFactory();
        sslContextFactory.setTrustStoreResource(resourceFactory.newResource(getClass().getResource("/stores/http2_client.jts")));
        sslContextFactory.setTrustStorePassword("http2_client");
        sslContextFactory.start();

        ClientConnector http1Connector = new ClientConnector();
        http1Connector.setSslContextFactory(sslContextFactory);

        http1Client = new HttpClient(new HttpClientTransportOverHTTP(http1Connector));
        http1Client.start();

        ClientConnector http2Connector = new ClientConnector();
        http2Connector.setSslContextFactory(sslContextFactory);

        http2Client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client(http2Connector)));
        http2Client.start();

    }

    @AfterEach
    void tearDown() throws Exception {
        http2Client.stop();
        http1Client.stop();
        sslContextFactory.stop();
    }

    static void assertResponse(ContentResponse response, HttpVersion httpVersion) {
        assertThat(response.getVersion()).isEqualTo(httpVersion);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(FakeApplication.HELLO_WORLD);
    }

    static boolean performManyAsyncRequests(HttpClient client, String url) throws InterruptedException {
        final int amount = 100;
        final CountDownLatch latch = new CountDownLatch(amount);
        for (int i = 0; i < amount; i++) {
            client.newRequest(url)
                    .send(new BufferingResponseListener() {
                        @Override
                        public void onComplete(Result result) {
                            assertThat(result.getResponse().getVersion()).isEqualTo(HttpVersion.HTTP_2);
                            assertThat(result.getResponse().getStatus()).isEqualTo(200);
                            assertThat(getContentAsString(StandardCharsets.UTF_8)).isEqualTo(FakeApplication.HELLO_WORLD);
                            latch.countDown();
                        }
                    });
        }

        return latch.await(30, TimeUnit.SECONDS);
    }
}
