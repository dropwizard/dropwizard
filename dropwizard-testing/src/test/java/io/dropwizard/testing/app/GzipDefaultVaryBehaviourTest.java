package io.dropwizard.testing.app;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.DropwizardAppRuleTest;
import io.dropwizard.testing.junit.TestApplication;
import io.dropwizard.testing.junit.TestConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

public class GzipDefaultVaryBehaviourTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, DropwizardAppRuleTest.resourceFilePath("test-config.yaml"));

    @Test
    public void testDefaultVaryHeader() {
        final ClientResponse clientResponse = new Client().resource("http://localhost:" +
                RULE.getLocalPort()
                +"/test")
                .header(ACCEPT_ENCODING, "gzip")
                .get(ClientResponse.class);

        assertThat(clientResponse.getHeaders().get(VARY)).isEqualTo(asList(ACCEPT_ENCODING));
        assertThat(clientResponse.getHeaders().get(CONTENT_ENCODING)).isEqualTo(asList("gzip"));
    }
}
