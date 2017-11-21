package io.dropwizard.testing.junit5;

import com.google.common.io.Resources;
import io.dropwizard.testing.app.TestResource;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
public class DropwizardClientExtensionTest {

    public static final DropwizardClientExtension EXTENSION_WITH_INSTANCE = new DropwizardClientExtension(new TestResource("foo"));
    public static final DropwizardClientExtension EXTENSION_WITH_CLASS = new DropwizardClientExtension(TestResource.class);

    @Test
    public void shouldGetStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_INSTANCE.baseUri() + "/test");
        assertThat("foo").isEqualTo(Resources.toString(url, StandardCharsets.UTF_8));
    }

    @Test
    public void shouldGetDefaultStringBodyFromDropWizard() throws IOException {
        final URL url = new URL(EXTENSION_WITH_CLASS.baseUri() + "/test");
        assertThat(Resources.toString(url, StandardCharsets.UTF_8)).isEqualTo(TestResource.DEFAULT_MESSAGE);
    }
}
