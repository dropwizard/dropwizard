package io.dropwizard.testing.junit;

import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.client.ClientBuilder;
import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class DropwizardAppRuleConfigOverrideTest {

    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
            new DropwizardAppRule<>(TestApplication.class, resourceFilePath("test-config.yaml"),
                    Optional.of("app-rule"), config("app-rule", "message", "A new way to say Hooray!"));

    @Test
    public void supportsConfigAttributeOverrides() {
        final String content = ClientBuilder.newClient().target("http://localhost:" + RULE.getLocalPort() + "/test")
                .request().get(String.class);

        assertThat(content, is("A new way to say Hooray!"));
    }
}
