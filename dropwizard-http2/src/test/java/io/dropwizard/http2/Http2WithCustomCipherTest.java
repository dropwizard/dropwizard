package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.junit.DropwizardAppRule;
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

    @Test
    public void testHttp2WithCustomCipher() throws Exception {
        assertResponse(client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"));
    }

}
