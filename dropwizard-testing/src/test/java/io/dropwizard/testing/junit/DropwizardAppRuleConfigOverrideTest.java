package io.dropwizard.testing.junit;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DropwizardAppRuleConfigOverrideTest {

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

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("test-config.yaml"),
                    Optional.of("app-rule"),
                    config("app-rule", "message", "A new way to say Hooray!"),
                    config("app-rule", "extra", () -> "supplied"),
                    config("extra", () -> "supplied again"));

    @Test
    public void supportsConfigAttributeOverrides() {
        final String content = client.target("http://localhost:" + RULE.getLocalPort() + "/test")
                .request().get(String.class);

        assertThat(content, is("A new way to say Hooray!"));
    }

    @Test
    public void supportsSuppliedConfigAttributeOverrides() throws Exception {
        assertThat(System.getProperty("app-rule.extra"), is("supplied"));
        assertThat(System.getProperty("dw.extra"), is("supplied again"));
    }
}
