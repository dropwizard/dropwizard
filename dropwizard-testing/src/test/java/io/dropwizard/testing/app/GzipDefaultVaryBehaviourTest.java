package io.dropwizard.testing.app;

import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.testing.junit.TestApplication;
import io.dropwizard.testing.junit.TestConfiguration;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import java.util.Collections;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.VARY;
import static org.assertj.core.api.Assertions.assertThat;

public class GzipDefaultVaryBehaviourTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("gzip-vary-test-config.yaml"));

    private Client client;

    @Before
    public void setUp() throws Exception {
        client = new JerseyClientBuilder()
            .property(ClientProperties.CONNECT_TIMEOUT, 1000)
            .property(ClientProperties.READ_TIMEOUT, 5000)
            .build();
    }

    @After
    public void tearDown() throws Exception {
        client.close();
    }

    @Test
    public void testDefaultVaryHeader() {
        final Response clientResponse = client.target("http://127.0.0.1:" + RULE.getLocalPort() + "/test")
            .request().header(ACCEPT_ENCODING, "gzip")
            .get();

        assertThat(clientResponse.getHeaders().get(VARY)).isEqualTo(Collections.singletonList((Object) ACCEPT_ENCODING));
        assertThat(clientResponse.getHeaders().get(CONTENT_ENCODING)).isEqualTo(Collections.singletonList((Object) "gzip"));
    }
}
