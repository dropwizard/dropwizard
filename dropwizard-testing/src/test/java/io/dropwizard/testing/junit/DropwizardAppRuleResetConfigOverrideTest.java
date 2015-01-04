package io.dropwizard.testing.junit;

import org.junit.Test;

import static io.dropwizard.testing.ResourceHelpers.resourceFilePath;
import static io.dropwizard.testing.ConfigOverride.config;
import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardAppRuleResetConfigOverrideTest {
    public final DropwizardAppRule<TestConfiguration> dropwizardAppRule = new DropwizardAppRule<>(
            TestApplication.class,
            resourceFilePath("test-config.yaml"),
            config("message", "A new way to say Hooray!"));

    @Test
    public void test2() throws Exception {
        dropwizardAppRule.before();
        assertThat(System.getProperty("dw.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("dw.extra")).isNull();
        dropwizardAppRule.after();

        System.setProperty("dw.extra", "Some extra system property");
        dropwizardAppRule.before();
        assertThat(System.getProperty("dw.message")).isEqualTo("A new way to say Hooray!");
        assertThat(System.getProperty("dw.extra")).isEqualTo("Some extra system property");
        dropwizardAppRule.after();

        assertThat(System.getProperty("dw.message")).isNull();
        assertThat(System.getProperty("dw.extra")).isEqualTo("Some extra system property");
    }
}
