package io.dropwizard.http2;

import com.google.common.base.Charsets;
import io.dropwizard.logging.BootstrapLogging;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpVersion;

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

        assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
    }
}
