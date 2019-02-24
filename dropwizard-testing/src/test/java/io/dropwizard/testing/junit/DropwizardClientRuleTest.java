package io.dropwizard.testing.junit;

import io.dropwizard.testing.app.TestResource;
import io.dropwizard.util.Resources;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class DropwizardClientRuleTest {
    @SuppressWarnings("deprecation")
    @ClassRule
    public static final DropwizardClientRule RULE_WITH_INSTANCE = new DropwizardClientRule(new TestResource("foo"));

    @SuppressWarnings("deprecation")
    @ClassRule
    public static final DropwizardClientRule RULE_WITH_CLASS = new DropwizardClientRule(TestResource.class);

    @Test
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(RULE_WITH_INSTANCE.baseUri() + "/test");
        assertThat("foo").isEqualTo(Resources.toString(url, StandardCharsets.UTF_8));
    }

    @Test
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(RULE_WITH_CLASS.baseUri() + "/test");
        assertThat(Resources.toString(url, StandardCharsets.UTF_8)).isEqualTo(TestResource.DEFAULT_MESSAGE);
    }
}
