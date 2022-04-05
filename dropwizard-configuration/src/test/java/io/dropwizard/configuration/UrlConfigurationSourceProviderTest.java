package io.dropwizard.configuration;

import io.dropwizard.util.ByteStreams;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class UrlConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new UrlConfigurationSourceProvider();

    @Test
    void readsFileContents() throws Exception {
        try (InputStream input = provider.open(getClass().getResource("/example.txt").toString())) {
            assertThat(new String(ByteStreams.toByteArray(input), StandardCharsets.UTF_8).trim())
                .isEqualTo("whee");
        }
    }
}
