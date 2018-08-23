package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.junit.Test;

public class SubstitutingSourceProviderTest {
    @Test
    public void shouldSubstituteCorrectly() throws IOException {
        StringLookup dummyLookup = (x) -> "baz";
        DummySourceProvider dummyProvider = new DummySourceProvider();
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(dummyProvider, new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: baz".getBytes(StandardCharsets.UTF_8)));

        // ensure that opened streams are closed
        assertThatThrownBy(() -> dummyProvider.lastStream.read())
                .isInstanceOf(IOException.class)
                .hasMessage("Stream closed");
    }

    @Test
    public void shouldSubstituteOnlyExistingVariables() throws IOException {
        StringLookup dummyLookup = (x) -> null;
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: ${bar}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldSubstituteWithDefaultValue() throws IOException {
        StringLookup dummyLookup = (x) -> null;
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StringSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar:-default}")).hasSameContentAs(new ByteArrayInputStream("foo: default".getBytes(StandardCharsets.UTF_8)));
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
