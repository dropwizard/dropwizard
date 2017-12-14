package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.http.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.security.Security;
import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

public class Http2WithConscrypt extends AbstractHttp2Test {

    static {
        Security.addProvider(new OpenSSLProvider());
    }

    private static final String PREFIX = "tls_conscrypt";

    @Rule
    public final DropwizardAppRule<Configuration> appRule = new DropwizardAppRule<>(
        FakeApplication.class, resourceFilePath("test-http2-with-conscrypt.yml"),
        Optional.of(PREFIX),
        config(PREFIX, "server.connector.keyStorePath", resourceFilePath("stores/http2_server.jks")),
        config(PREFIX, "server.connector.trustStorePath", resourceFilePath("stores/http2_client.jts"))
    );

    @Test
    public void testHttp2WithCustomCipher() throws Exception {
        assertResponse(client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"));
    }

}
