package io.dropwizard.http2;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import java.security.Security;
import org.conscrypt.OpenSSLProvider;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2WithConscryptTest extends Http2TestCommon {

    static {
        Security.addProvider(new OpenSSLProvider());
    }

    private static final String PREFIX = "tls_conscrypt";

    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
            FakeApplication.class,
            "test-http2-with-conscrypt.yml",
            new ResourceConfigurationSourceProvider(),
            PREFIX,
            config(PREFIX, "server.connector.keyStorePath", resourceFilePath("stores/http2_server.jks")),
            config(PREFIX, "server.connector.trustStorePath", resourceFilePath("stores/http2_client.jts")));

    @Test
    void testHttp1WithCustomCipher() throws Exception {
        assertResponse(
                http1Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_1_1);
    }

    @Test
    void testHttp2WithCustomCipher() throws Exception {
        assertResponse(
                http2Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_2);
    }
}
