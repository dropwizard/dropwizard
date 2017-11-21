package io.dropwizard.testing.junit5;


import io.dropwizard.testing.app.TestResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Assertions;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardClientExtensionTest {
    public static final DropwizardClientExtension EXTENSION_WITH_INSTANCE = new DropwizardClientExtension(new TestResource("foo"));

    public static final DropwizardClientExtension EXTENSION_WITH_CLASS = new DropwizardClientExtension(TestResource.class);

    @Test
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_INSTANCE.baseUri() + "/test");
        final String response = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)).readLine();
        Assertions.assertEquals("foo", response);
    }

    @Test
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_CLASS.baseUri() + "/test");
        final String response = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)).readLine();
        Assertions.assertEquals(TestResource.DEFAULT_MESSAGE, response);
    }
}
