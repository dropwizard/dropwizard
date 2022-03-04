package io.dropwizard.http2;

import io.dropwizard.core.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2IntegrationTest extends Http2TestCommon {

    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
        FakeApplication.class, "test-http2.yml",
        new ResourceConfigurationSourceProvider(),
        "tls_http2",
        ConfigOverride.config("tls_http2", "server.connector.keyStorePath",
            resourceFilePath("stores/http2_server.jks")),
        ConfigOverride.config("tls_http2", "server.connector.trustStorePath",
            resourceFilePath("stores/http2_client.jts"))
    );

    @Test
    void testHttp1() throws Exception {
        assertResponse(http1Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_1_1);
    }

    @Test
    void testHttp2() throws Exception {
        assertResponse(http2Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_2);
    }

    @Test
    void testHttp2ManyRequests() throws Exception {
        assertThat(performManyAsyncRequests(http2Client, "https://localhost:" + appRule.getLocalPort() + "/api/test"))
            .isTrue();
    }
}
