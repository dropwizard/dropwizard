package io.dropwizard.configuration;

import org.apache.commons.text.StrSubstitutor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static java.util.Objects.requireNonNull;

/**
 * A delegating {@link ConfigurationSourceProvider} which replaces variables in the underlying configuration
 * source according to the rules of a custom {@link org.apache.commons.text.StrSubstitutor}.
 */
public class SubstitutingSourceProvider implements ConfigurationSourceProvider {
    private final ConfigurationSourceProvider delegate;
    private final StrSubstitutor substitutor;

    /**
     * Create a new instance.
     *
     * @param delegate    The underlying {@link io.dropwizard.configuration.ConfigurationSourceProvider}.
     * @param substitutor The custom {@link org.apache.commons.text.StrSubstitutor} implementation.
     */
    public SubstitutingSourceProvider(ConfigurationSourceProvider delegate, StrSubstitutor substitutor) {
        this.delegate = requireNonNull(delegate);
        this.substitutor = requireNonNull(substitutor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream open(String path) throws IOException {
        try (InputStream in = delegate.open(path);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            final String config = outputStream.toString(StandardCharsets.UTF_8.name());
            final String substituted = substitutor.replace(config);

            return new ByteArrayInputStream(substituted.getBytes(StandardCharsets.UTF_8));
        }
    }
}
