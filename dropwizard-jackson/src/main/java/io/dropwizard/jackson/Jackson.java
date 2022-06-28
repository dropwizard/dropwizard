package io.dropwizard.jackson;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * A utility class for Jackson.
 */
public class Jackson {
    private Jackson() { /* singleton */ }

    /**
     * Creates a new {@link ObjectMapper} with Guava, Logback, and Joda Time support, as well as
     * support for {@link JsonSnakeCase}. Also includes all {@link Discoverable} interface implementations.
     */
    public static ObjectMapper newObjectMapper() {
        return newObjectMapper(Jackson::configure);
    }

    /**
     * Creates a new {@link ObjectMapper} configured with the given {@link Consumer} instance.
     *
     * @param configurer the {@link Consumer} to configure the {@link ObjectMapper}
     */
    public static ObjectMapper newObjectMapper(Consumer<ObjectMapper> configurer) {
        final ObjectMapper mapper = new ObjectMapper();
        configurer.accept(mapper);
        return mapper;
    }

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link com.fasterxml.jackson.core.JsonFactory}
     * with Guava, Logback, and Joda Time support, as well as support for {@link JsonSnakeCase}.
     * Also includes all {@link Discoverable} interface implementations.
     *
     * @param jsonFactory instance of {@link com.fasterxml.jackson.core.JsonFactory} to use
     *                    for the created {@link com.fasterxml.jackson.databind.ObjectMapper} instance.
     */
    public static ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory) {
        return newObjectMapper(jsonFactory, Jackson::configure);
    }

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link JsonFactory} configured with the given {@link Consumer} instance.
     *
     * @param jsonFactory instance of {@link com.fasterxml.jackson.core.JsonFactory} to use
     *                    for the created {@link com.fasterxml.jackson.databind.ObjectMapper} instance.
     * @param configurer the {@link Consumer} to configure the {@link ObjectMapper}
     */
    public static ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory, Consumer<ObjectMapper> configurer) {
        final ObjectMapper mapper = new ObjectMapper(jsonFactory);
        configurer.accept(mapper);
        return mapper;
    }

    /**
     * Creates a new minimal {@link ObjectMapper} that will work with Dropwizard out of box.
     * <p><b>NOTE:</b> Use it, if the default Dropwizard's {@link ObjectMapper}, created in
     * {@link #newObjectMapper()}, is too aggressive for you.</p>
     */
    public static ObjectMapper newMinimalObjectMapper() {
        return new ObjectMapper()
                .registerModule(new GuavaModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static ObjectMapper configure(ObjectMapper mapper) {
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new GuavaExtrasModule());
        mapper.registerModule(new CaffeineModule());
        mapper.registerModule(new JodaModule());
        mapper.registerModule(new BlackbirdModule());
        mapper.registerModule(new FuzzyEnumModule());
        mapper.registerModule(new ParameterNamesModule());
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.setPropertyNamingStrategy(new AnnotationSensitivePropertyNamingStrategy());
        mapper.setSubtypeResolver(new DiscoverableSubtypeResolver());
        mapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);

        return mapper;
    }
}
