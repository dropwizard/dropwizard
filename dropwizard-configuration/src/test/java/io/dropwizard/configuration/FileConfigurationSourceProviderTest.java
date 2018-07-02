package io.dropwizard.configuration;

import io.dropwizard.util.Resources;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class FileConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new FileConfigurationSourceProvider();

    @Test
    public void readsFileContents() throws Exception {
        try (InputStream input = provider.open(Resources.getResource("example.txt").getFile());
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }

            assertThat(new String(output.toByteArray(), StandardCharsets.UTF_8).trim()).isEqualTo("whee");
        }
    }
}
