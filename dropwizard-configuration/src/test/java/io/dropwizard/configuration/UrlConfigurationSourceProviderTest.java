package io.dropwizard.configuration;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class UrlConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new UrlConfigurationSourceProvider();

    @Test
    void readsFileContents() throws Exception {
        try (InputStream input = provider.open(getClass().getResource("/example.txt").toString())) {
            assertThat(new String(input.readAllBytes(), StandardCharsets.UTF_8).trim())
                .isEqualTo("whee");
        }
    }
}
