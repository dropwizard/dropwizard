package io.dropwizard.testing.junit;

import static org.junit.Assert.*;

import java.io.*;
import java.net.URL;

import org.junit.*;

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
