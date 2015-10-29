package io.dropwizard.configuration;

import com.google.common.io.ByteStreams;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class ResourceConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new ResourceConfigurationSourceProvider();

    @Test
    public void readsFileContents() throws Exception {
        try (InputStream input = provider.open("example.txt")) {
            assertThat(new String(ByteStreams.toByteArray(input), StandardCharsets.UTF_8).trim()).isEqualTo("whee");
        }
    }
}
