package io.dropwizard.configuration;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ResourceConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new ResourceConfigurationSourceProvider();

    @ParameterizedTest
    @ValueSource(
            strings = {
                "example.txt",
                "io/dropwizard/configuration/not-root-example.txt",
                "/io/dropwizard/configuration/not-root-example.txt"
            })
    void readsFileContents(String path) throws Exception {
        assertThat(provider.open(path)).asString(UTF_8).isEqualToIgnoringWhitespace("whee");
    }
}
