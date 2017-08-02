package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class Http2WithCustomCipherTest extends AbstractHttp2Test {

    private static final String PREFIX = "tls_custom_http2";

    @Rule
    public final DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
        FakeApplication.class, resourceFilePath("test-http2-with-custom-cipher.yml"),
        Optional.of(PREFIX),
        config(PREFIX, "server.connector.keyStorePath", resourceFilePath("stores/http2_server.jks")),
        config(PREFIX, "server.connector.trustStorePath", resourceFilePath("stores/http2_client.jts"))
    );

    private final SslContextFactory sslContextFactory = new SslContextFactory();
    private HttpClient client;

    @Before
    public void setUp() throws Exception {
        sslContextFactory.setTrustStorePath(resourceFilePath("stores/http2_client.jts"));
        sslContextFactory.setTrustStorePassword("http2_client");
        sslContextFactory.start();

        client = new HttpClient(new HttpClientTransportOverHTTP2(new HTTP2Client()), sslContextFactory);
        client.start();
    }

    @After
    public void tearDown() throws Exception {
        client.stop();
    }

    @Test
    public void testHttp2WithCustomCipher() throws Exception {
        assertResponse(client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"));
    }

}
