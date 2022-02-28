package io.dropwizard.http2;

import io.dropwizard.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.eclipse.jetty.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class Http2IntegrationTest extends AbstractHttp2Test {

    final DropwizardAppExtension<Configuration> appRule = new DropwizardAppExtension<>(
        FakeApplication.class, ResourceHelpers.resourceFilePath("test-http2.yml"),
        "tls_http2",
        ConfigOverride.config("tls_http2", "server.connector.keyStorePath",
            ResourceHelpers.resourceFilePath("stores/http2_server.jks")),
        ConfigOverride.config("tls_http2", "server.connector.trustStorePath",
            ResourceHelpers.resourceFilePath("stores/http2_client.jts"))
    );

    @Test
    void testHttp1() throws Exception {
        AbstractHttp2Test.assertResponse(http1Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_1_1);
    }

    @Test
    void testHttp2() throws Exception {
        AbstractHttp2Test.assertResponse(http2Client.GET("https://localhost:" + appRule.getLocalPort() + "/api/test"), HttpVersion.HTTP_2);
    }

    @Test
    void testHttp2ManyRequests() throws Exception {
        assertThat(AbstractHttp2Test.performManyAsyncRequests(http2Client, "https://localhost:" + appRule.getLocalPort() + "/api/test"))
            .isTrue();
    }
}
