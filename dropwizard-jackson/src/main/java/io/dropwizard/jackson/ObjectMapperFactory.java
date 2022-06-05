package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;

/**
 * Factory for creating instances of Jackson's {@link ObjectMapper}.
 *
 * @since 2.1
 */
public interface ObjectMapperFactory {
    /**
     * Creates a new {@link ObjectMapper}.
     *
     * @since 2.1
     */
    ObjectMapper newObjectMapper();

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link JsonFactory}.
     *
     * @param jsonFactory instance of {@link JsonFactory} to use for the created {@link ObjectMapper} instance.
     * @since 2.1
     */
    ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory);
}
