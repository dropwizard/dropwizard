package io.dropwizard.testing.junit;

import org.junit.ClassRule;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import static org.junit.Assert.assertEquals;

public class DropwizardClientRuleTest {
    @ClassRule
    public static final DropwizardClientRule RULE_WITH_INSTANCE = new DropwizardClientRule(new TestResource("foo"));

    @ClassRule
    public static final DropwizardClientRule RULE_WITH_CLASS = new DropwizardClientRule(TestResource.class);

    @Test(timeout = 5000)
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(RULE_WITH_INSTANCE.baseUri() + "/test");
        final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        assertEquals("foo", response);
    }

    @Test(timeout = 5000)
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(RULE_WITH_CLASS.baseUri() + "/test");
        final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        assertEquals(TestResource.DEFAULT_MESSAGE, response);
    }
}
