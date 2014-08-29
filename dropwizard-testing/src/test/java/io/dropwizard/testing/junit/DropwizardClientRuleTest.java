package io.dropwizard.testing.junit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.junit.ClassRule;
import org.junit.Test;

public class DropwizardClientRuleTest {
    @ClassRule
    public static DropwizardClientRule dropwizard = new DropwizardClientRule(new TestResource("foo"));

    @Test(timeout = 5000)
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        URL url = new URL(dropwizard.baseUri() + "/test");
        String response = new BufferedReader(new InputStreamReader(url.openStream())).readLine();
        assertEquals("foo", response);
    }
}
