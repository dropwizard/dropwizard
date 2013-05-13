package com.codahale.dropwizard.configuration;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.InputStream;

import static org.fest.assertions.api.Assertions.assertThat;

public class FileConfigurationSourceProviderTest {
    private final ConfigurationSourceProvider provider = new FileConfigurationSourceProvider();

    @Test
    public void readsFileContents() throws Exception {
        try (InputStream input = provider.open(Resources.getResource("example.txt").getFile())) {
            assertThat(new String(ByteStreams.toByteArray(input), Charsets.UTF_8).trim())
                    .isEqualTo("whee");
        }
    }
}
