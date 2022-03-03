package io.dropwizard.testing.junit;

import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.ClassRule;
import org.junit.Test;

import static io.dropwizard.testing.ConfigOverride.config;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardAppRuleConfigOverrideTest {
    @SuppressWarnings("deprecation")
    @ClassRule
    public static final DropwizardAppRule<TestConfiguration> RULE =
        new DropwizardAppRule<>(TestApplication.class, "test-config.yaml",
            new ResourceConfigurationSourceProvider(),
            "app-rule",
            config("app-rule", "message", "A new way to say Hooray!"),
            config("app-rule", "extra", () -> "supplied"),
            config("extra", () -> "supplied again"));

    @Test
    public void supportsConfigAttributeOverrides() {
        final String content = RULE.client().target("http://localhost:" + RULE.getLocalPort() + "/test")
            .request().get(String.class);

        assertThat(content).isEqualTo("A new way to say Hooray!");
    }

    @Test
    public void supportsSuppliedConfigAttributeOverrides() {
        assertThat(System.getProperties())
            .containsEntry("app-rule.extra", "supplied")
            .containsEntry("dw.extra", "supplied again");
    }
}
