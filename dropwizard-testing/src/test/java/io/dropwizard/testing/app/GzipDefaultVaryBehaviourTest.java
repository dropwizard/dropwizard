package io.dropwizard.testing.app;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.junit.DropwizardAppRule;
import jakarta.ws.rs.core.Response;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Collections;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static jakarta.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

public class GzipDefaultVaryBehaviourTest {

    @SuppressWarnings("deprecation")
    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, "gzip-vary-test-config.yaml", new ResourceConfigurationSourceProvider());

    @Test
    public void testDefaultVaryHeader() {
        final Response clientResponse = RULE.client().target(
            "http://localhost:" + RULE.getLocalPort() + "/test").request().header(ACCEPT_ENCODING, "gzip").get();

        assertThat(clientResponse.getHeaders())
            .containsEntry(VARY, Collections.singletonList(ACCEPT_ENCODING))
            .containsEntry(CONTENT_ENCODING, Collections.singletonList("gzip"));
    }
}
