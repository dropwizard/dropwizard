package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.HTTP2ClientConnectionFactory;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2CIntegrationTest extends AbstractHttp2Test {

    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
            FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2c.yml"));

    @BeforeEach
    @Override
    void setUp() throws Exception {
        final HTTP2Client http2Client = new HTTP2Client();
        http2Client.setClientConnectionFactory(new HTTP2ClientConnectionFactory()); // No need for ALPN
        this.http2Client = new HttpClient(new HttpClientTransportOverHTTP2(http2Client), null);
        this.http2Client.start();

        this.http1Client = new HttpClient();
        this.http1Client.start();
    }

    @AfterEach
    @Override
    void tearDown() throws Exception {
        http2Client.stop();
        http1Client.stop();
    }

    @Test
    void testHttp1() throws Exception {
        assertResponse(http1Client.GET("http://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_1_1);
    }

    @Test
    void testHttp2c() throws Exception {
        assertResponse(http2Client.GET("http://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_2);
    }

    @Test
    void testHttp2cManyRequests() throws Exception {
        performManyAsyncRequests(http2Client, "http://localhost:" + appRule.getLocalPort() + "/api/test");
    }
}
