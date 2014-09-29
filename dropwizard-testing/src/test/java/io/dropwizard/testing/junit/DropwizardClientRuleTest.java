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
    public static final DropwizardClientRule RULE = new DropwizardClientRule(new TestResource("foo"));

    @Test(timeout = 5000)
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(RULE.baseUri() + "/test");
        final String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        assertEquals("foo", response);
    }
}
