package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

import javax.annotation.Nullable;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * Base class for a factory for creating instances of the {@link ObjectMapper} instances for Dropwizard.
 *
 * @since 2.1.1
 */
public abstract class BaseObjectMapperFactory implements ObjectMapperFactory {
    /**
     * Creates a new {@link ObjectMapper} with a custom {@link JsonFactory}
     * with Guava, Logback, and Joda Time support, as well as support for {@link JsonSnakeCase}.
     * Also includes all {@link Discoverable} interface implementations.
     */
    @Override
    public ObjectMapper newObjectMapper() {
        return configure(new ObjectMapper());
    }

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link JsonFactory}
     * with Guava, Logback, and Joda Time support, as well as support for {@link JsonSnakeCase}.
     * Also includes all {@link Discoverable} interface implementations.
     *
     * @param jsonFactory instance of {@link JsonFactory} to use
     *                    for the created {@link ObjectMapper} instance.
     */
    @Override
    public ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory) {
        return configure(new ObjectMapper(jsonFactory));
    }

    /**
     * Creates a new minimal {@link ObjectMapper} that will work with Dropwizard out of box.
     * <p><b>NOTE:</b> Use it, if the default Dropwizard's {@link ObjectMapper}, created in
     * {@link #newObjectMapper()}, is too aggressive for you.</p>
     */
    public ObjectMapper newMinimalObjectMapper() {
        return new ObjectMapper()
                .registerModule(new GuavaModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Configure the provided {@link ObjectMapper} instance.
     *
     * @param mapper The {@link ObjectMapper} instance to customize.
     * @return The customized {@link ObjectMapper} instance.
     */
    protected ObjectMapper configure(ObjectMapper mapper) {
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new GuavaExtrasModule());
        mapper.registerModule(new CaffeineModule());
        mapper.registerModule(new JodaModule());
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
