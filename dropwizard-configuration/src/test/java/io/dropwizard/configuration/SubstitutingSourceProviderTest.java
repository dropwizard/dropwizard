package io.dropwizard.configuration;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class SubstitutingSourceProviderTest {
    @Test
    void shouldSubstituteCorrectly() throws IOException {
        StringLookup dummyLookup = (x) -> "baz";
        DummySourceProvider dummyProvider = new DummySourceProvider();
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(dummyProvider, new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: baz".getBytes(StandardCharsets.UTF_8)));

        // ensure that opened streams are closed
        assertThatExceptionOfType(IOException.class)
            .isThrownBy(() -> dummyProvider.lastStream.read())
            .withMessage("Stream closed");
    }

    @Test
    void shouldSubstituteOnlyExistingVariables() throws IOException {
        StringLookup dummyLookup = (x) -> null;
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: ${bar}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldSubstituteWithDefaultValue() throws IOException {
        StringLookup dummyLookup = (x) -> null;
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar:-default}")).hasSameContentAs(new ByteArrayInputStream("foo: default".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void shouldNotBeVulnerableToCVE_2022_42889() throws IOException {
        StringLookup dummyLookup = (x) -> null;
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${script:javascript:3 + 4}")).hasSameContentAs(new ByteArrayInputStream("foo: ${script:javascript:3 + 4}".getBytes(StandardCharsets.UTF_8)));
    }

    private static class DummySourceProvider implements ConfigurationSourceProvider {
        InputStream lastStream = new ByteArrayInputStream(new byte[0]);

        @Override
        public InputStream open(String s) throws IOException {
            // used to test that the stream is properly closed
            lastStream = new BufferedInputStream(new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8)));
            return lastStream;
        }
    }
}
