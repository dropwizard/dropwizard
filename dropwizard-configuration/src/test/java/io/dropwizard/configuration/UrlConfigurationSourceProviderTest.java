package io.dropwizard.configuration;

import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new UrlConfigurationSourceProvider();

    @Test
    public void readsFileContents() throws Exception {
        try (InputStream input = provider.open(Resources.getResource("example.txt").toString())) {
            assertThat(new String(ByteStreams.toByteArray(input), StandardCharsets.UTF_8).trim())
                    .isEqualTo("whee");
        }
    }
}
