package io.dropwizard.http2;

import io.dropwizard.logging.common.BootstrapLogging;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.jetty.util.resource.Resource.newResource;

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
        sslContextFactory.setTrustStoreResource(newResource(getClass().getResource("/stores/http2_client.jts")));
        sslContextFactory.setTrustStorePassword("http2_client");
        sslContextFactory.start();

        http1Client = new HttpClient(sslContextFactory);
        http1Client.start();

        http2Client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()), sslContextFactory);
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
