package io.dropwizard.http2;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2CIntegrationTest extends Http2TestCommon {

    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
            FakeApplication.class, "test-http2c.yml", new ResourceConfigurationSourceProvider());

    @BeforeEach
    @Override
    void setUp() throws Exception {
        final HTTP2Client http2Client = new HTTP2Client();
        HttpClientTransportOverHTTP2 httpClientTransportOverHTTP2 = new HttpClientTransportOverHTTP2(http2Client);
        httpClientTransportOverHTTP2.setUseALPN(false);
        this.http2Client = new HttpClient(httpClientTransportOverHTTP2);
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
        assertThat(performManyAsyncRequests(http2Client, "http://localhost:" + appRule.getLocalPort() + "/api/test"))
            .isTrue();
    }
}
