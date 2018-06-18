package io.dropwizard.configuration;

import org.apache.commons.text.StrLookup;
import org.apache.commons.text.StrSubstitutor;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SubstitutingSourceProviderTest {
    @Test
    public void shouldSubstituteCorrectly() throws IOException {
        StrLookup<?> dummyLookup = new StrLookup<Object>() {
            @Override
            public String lookup(String key) {
                return "baz";
            }
        };
        DummySourceProvider dummyProvider = new DummySourceProvider();
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(dummyProvider, new StrSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: baz".getBytes(StandardCharsets.UTF_8)));

        // ensure that opened streams are closed
        assertThatThrownBy(() -> dummyProvider.lastStream.read())
                .isInstanceOf(IOException.class)
                .hasMessage("Stream closed");
    }

    @Test
    public void shouldSubstituteOnlyExistingVariables() throws IOException {
        StrLookup<?> dummyLookup = new StrLookup<Object>() {
            @Override
            @Nullable
            public String lookup(String key) {
                return null;
            }
        };
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StrSubstitutor(dummyLookup));

        assertThat(provider.open("foo: ${bar}")).hasSameContentAs(new ByteArrayInputStream("foo: ${bar}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void shouldSubstituteWithDefaultValue() throws IOException {
        StrLookup<?> dummyLookup = new StrLookup<Object>() {
            @Override
            @Nullable
            public String lookup(String key) {
                return null;
            }
        };
        SubstitutingSourceProvider provider = new SubstitutingSourceProvider(new DummySourceProvider(), new StrSubstitutor(dummyLookup));

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
