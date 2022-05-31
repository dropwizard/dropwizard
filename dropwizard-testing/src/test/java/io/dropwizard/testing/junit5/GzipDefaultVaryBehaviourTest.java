package io.dropwizard.testing.junit5;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collections;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static jakarta.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class GzipDefaultVaryBehaviourTest {

    private final DropwizardAppExtension<TestConfiguration> extension = new DropwizardAppExtension<>(TestApplication.class,
        "gzip-vary-test-config.yaml", new ResourceConfigurationSourceProvider());

    @Test
    void testDefaultVaryHeader() {
        final Response clientResponse = extension.client().target(
            "http://localhost:" + extension.getLocalPort() + "/test").request().header(ACCEPT_ENCODING, "gzip").get();

        assertThat(clientResponse.getHeaders())
            .containsEntry(VARY, Collections.singletonList(ACCEPT_ENCODING))
            .containsEntry(CONTENT_ENCODING, Collections.singletonList("gzip"));
    }
}
