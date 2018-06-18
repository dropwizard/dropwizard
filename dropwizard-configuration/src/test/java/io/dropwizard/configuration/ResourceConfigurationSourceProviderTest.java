package io.dropwizard.configuration;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new ResourceConfigurationSourceProvider();

    @Test
    public void readsFileContents() throws Exception {
        assertForWheeContent("example.txt");
        assertForWheeContent("io/dropwizard/configuration/not-root-example.txt");
        assertForWheeContent("/io/dropwizard/configuration/not-root-example.txt");
    }

    private void assertForWheeContent(String path) throws Exception {
        assertThat(loadResourceAsString(path)).isEqualToIgnoringWhitespace("whee");
    }

    private String loadResourceAsString(String path) throws Exception {
        try (InputStream inputStream = provider.open(path);
             ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString(StandardCharsets.UTF_8.name());
        }
    }
}
