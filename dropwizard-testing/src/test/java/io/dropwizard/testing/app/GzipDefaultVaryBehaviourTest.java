package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.TestApplication;
import io.dropwizard.testing.junit.TestConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

public class GzipDefaultVaryBehaviourTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("gzip-vary-test-config.yaml"));

    @Test
    public void testDefaultVaryHeader() {
        final Response clientResponse = ClientBuilder.newClient().target("http://localhost:" +
                RULE.getLocalPort()
                +"/test")
                .request()
                .header(ACCEPT_ENCODING, "gzip")
                .get();

        assertThat(clientResponse.getHeaders().get(VARY)).isEqualTo(asList((Object)ACCEPT_ENCODING));
        assertThat(clientResponse.getHeaders().get(CONTENT_ENCODING)).isEqualTo(asList((Object) "gzip"));
    }
}
