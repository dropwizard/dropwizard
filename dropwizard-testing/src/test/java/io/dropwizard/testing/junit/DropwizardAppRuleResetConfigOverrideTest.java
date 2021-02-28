package io.dropwizard.testing.junit;

import io.dropwizard.testing.app.TestApplication;
import io.dropwizard.testing.app.TestConfiguration;
import org.junit.Test;

import static io.dropwizard.testing.ConfigOverride.config;
import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardAppRuleResetConfigOverrideTest {
    @SuppressWarnings("deprecation")
    private final DropwizardAppRule<TestConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
        TestApplication.class,
        resourceFilePath("test-config.yaml"),
        "app-rule-reset",
        config("app-rule-reset", "message", "A new way to say Hooray!"));

    @Test
    public void test2() throws Exception {
        dropwizardAppRule.before();
        assertThat(System.getProperty("app-rule-reset.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("app-rule-reset.extra")).isNull();
        dropwizardAppRule.after();

        System.setProperty("app-rule-reset.extra", "Some extra system property");
        dropwizardAppRule.before();
        assertThat(System.getProperty("app-rule-reset.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("app-rule-reset.extra")).isEqualTo("Some extra system property");
        dropwizardAppRule.after();

        assertThat(System.getProperty("app-rule-reset.message")).isNull();
        assertThat(System.getProperty("app-rule-reset.extra")).isEqualTo("Some extra system property");
        System.clearProperty("app-rule-reset.extra");
    }
}
