package io.dropwizard.http2;

import com.google.common.base.Charsets;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.testing.ResourceHelpers;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Common code for HTTP/2 connector tests
 */
public class AbstractHttp2Test {

    static {
        BootstrapLogging.bootstrap();
    }

    final SslContextFactory sslContextFactory = new SslContextFactory();
    HttpClient client;

    @Before
    public void setUp() throws Exception {
        sslContextFactory.setTrustStorePath(ResourceHelpers.resourceFilePath("stores/http2_client.jts"));
        sslContextFactory.setTrustStorePassword("http2_client");
        sslContextFactory.start();

        client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()), sslContextFactory);
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
    }

    protected static void assertResponse(ContentResponse response) {
        assertThat(response.getVersion()).isEqualTo(HttpVersion.HTTP_2);
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).isEqualTo(FakeApplication.HELLO_WORLD);
    }

    protected void performManyAsyncRequests(HttpClient client, String url) throws InterruptedException {
        final int amount = 100;
        final CountDownLatch latch = new CountDownLatch(amount);
        for (int i = 0; i < amount; i++) {
            client.newRequest(url)
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

        assertThat(latch.await(30, TimeUnit.SECONDS)).isTrue();
    }
}
