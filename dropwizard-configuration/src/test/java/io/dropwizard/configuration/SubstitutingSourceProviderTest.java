package io.dropwizard.configuration;

import com.google.common.io.ByteStreams;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class SubstitutingSourceProviderTest {
    @Test
    public void shouldSubstituteCorrectly() throws IOException {
        StrLookup dummyLookup = new StrLookup() {
            @Override
            public String lookup(String key) {
                return "baz";
            }
        };
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StrSubstitutor(dummyLookup));
        String results = new String(ByteStreams.toByteArray(provider.open("foo: ${bar}")), StandardCharsets.UTF_8);

        assertThat(results).isEqualTo("foo: baz");
    }

    @Test
    public void shouldSubstituteOnlyExistingVariables() throws IOException {
        StrLookup dummyLookup = new StrLookup() {
            @Override
            public String lookup(String key) {
                return null;
            }
        };
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StrSubstitutor(dummyLookup));
        String results = new String(ByteStreams.toByteArray(provider.open("foo: ${bar}")), StandardCharsets.UTF_8);

        assertThat(results).isEqualTo("foo: ${bar}");
    }

    @Test
    public void shouldSubstituteWithDefaultValue() throws IOException {
        StrLookup dummyLookup = new StrLookup() {
            @Override
            public String lookup(String key) {
                return null;
            }
        };
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StrSubstitutor(dummyLookup));
        String results = new String(ByteStreams.toByteArray(provider.open("foo: ${bar:-default}")), StandardCharsets.UTF_8);

        assertThat(results).isEqualTo("foo: default");
    }

    private static class DummySourceProvider implements ConfigurationSourceProvider {
        @Override
        public InputStream open(String s) throws IOException {
            return new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
        }
    }
}
