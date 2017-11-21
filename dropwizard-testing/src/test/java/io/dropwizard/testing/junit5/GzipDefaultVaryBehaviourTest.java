package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.core.Response;
import java.util.Collections;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class GzipDefaultVaryBehaviourTest {

    private DropwizardAppExtension<TestConfiguration> extension = new DropwizardAppExtension<>(TestApplication.class,
        resourceFilePath("gzip-vary-test-config.yaml"));

    @Test
    public void testDefaultVaryHeader() {
        final Response clientResponse = extension.client().target(
            "http://localhost:" + extension.getLocalPort() + "/test").request().header(ACCEPT_ENCODING, "gzip").get();

        assertThat(clientResponse.getHeaders().get(VARY)).isEqualTo(Collections.singletonList((Object) ACCEPT_ENCODING));
        assertThat(clientResponse.getHeaders().get(CONTENT_ENCODING)).isEqualTo(Collections.singletonList((Object) "gzip"));
    }
}
