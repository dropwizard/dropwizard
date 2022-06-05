package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import javax.annotation.Nullable;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * A utility class for Jackson.
 *
 * @deprecated See {@link DefaultObjectMapperFactory}.
 */
@Deprecated
public class Jackson {
    private static final ObjectMapperFactory FACTORY = new DefaultObjectMapperFactory();

    private Jackson() { /* singleton */ }

    /**
     * Creates a new {@link ObjectMapper} with Guava, Logback, and Joda-Time support, as well as
     * support for {@link JsonSnakeCase}. Also includes all {@link Discoverable} interface implementations.
     *
     * @deprecated Use {@link ObjectMapperFactory#newObjectMapper()}.
     */
    @Deprecated
    public static ObjectMapper newObjectMapper() {
        return FACTORY.newObjectMapper();
    }

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link com.fasterxml.jackson.core.JsonFactory}
     * with Guava, Logback, and Joda-Time support, as well as support for {@link JsonSnakeCase}.
     * Also includes all {@link Discoverable} interface implementations.
     *
     * @param jsonFactory instance of {@link com.fasterxml.jackson.core.JsonFactory} to use
     *                    for the created {@link com.fasterxml.jackson.databind.ObjectMapper} instance.
     * @deprecated Use {@link ObjectMapperFactory#newObjectMapper(JsonFactory)}.
     */
    @Deprecated
    public static ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory) {
        return FACTORY.newObjectMapper(jsonFactory);
    }

    /**
     * Creates a new minimal {@link ObjectMapper} that will work with Dropwizard out of box.
     * <p><b>NOTE:</b> Use it, if the default Dropwizard's {@link ObjectMapper}, created in
     * {@link #newObjectMapper()}, is too aggressive for you.</p>
     */
    @Deprecated
    public static ObjectMapper newMinimalObjectMapper() {
        return new ObjectMapper()
                .registerModule(new GuavaModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
