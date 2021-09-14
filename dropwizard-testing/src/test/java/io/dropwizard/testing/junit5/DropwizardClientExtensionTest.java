package io.dropwizard.testing.junit5;

import io.dropwizard.testing.app.TestResource;
import io.dropwizard.util.Resources;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class DropwizardClientExtensionTest {

    private static final DropwizardClientExtension EXTENSION_WITH_INSTANCE = new DropwizardClientExtension(new TestResource("foo"));
    private static final DropwizardClientExtension EXTENSION_WITH_CLASS = new DropwizardClientExtension(TestResource.class);

    @Test
    void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_INSTANCE.baseUri() + "/test");
        assertThat(Resources.toString(url, StandardCharsets.UTF_8)).isEqualTo("foo");;
    }

    @Test
    void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_CLASS.baseUri() + "/test");
        assertThat(Resources.toString(url, StandardCharsets.UTF_8)).isEqualTo(TestResource.DEFAULT_MESSAGE);
    }
}
