package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * A utility class for Jackson.
 */
public class Jackson {
    private Jackson() { /* singleton */ }

    /**
     * Creates a new {@link ObjectMapper} with Guava and Logback support, as well as support for
     * {@link JsonSnakeCase}. Also includes all {@link Discoverable} interface implementations.
     *
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper newObjectMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        return configure(mapper);
    }

    /**
     * Creates a new {@link ObjectMapper} with a custom {@link com.fasterxml.jackson.core.JsonFactory}
     * with Guava and Logback support, as well as support for {@link JsonSnakeCase}.
     * Also includes all {@link Discoverable} interface implementations.
     *
     * @param jsonFactory instance of {@link com.fasterxml.jackson.core.JsonFactory} to use
     *                    for the created {@link com.fasterxml.jackson.databind.ObjectMapper} instance.
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper newObjectMapper(@Nullable JsonFactory jsonFactory) {
        final ObjectMapper mapper = new ObjectMapper(jsonFactory);

        return configure(mapper);
    }

    /**
     * Creates a new minimal {@link ObjectMapper} that will work with Dropwizard out of box.
     * <p><b>NOTE:</b> Use it, if the default Dropwizard's {@link ObjectMapper}, created in
     * {@link #newObjectMapper()}, is too aggressive for you.</p>
     *
     * @return the configured {@link ObjectMapper}
     */
    public static ObjectMapper newMinimalObjectMapper() {
        return new ObjectMapper()
                .registerModule(new GuavaModule())
                .setSubtypeResolver(new DiscoverableSubtypeResolver())
                .disable(FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /**
     * Configures an {@link ObjectMapper} with a set of common modules and properties.
     *
     * @param mapper the {@link ObjectMapper} to configure
     * @return the configured {@link ObjectMapper}
     */
    private static ObjectMapper configure(ObjectMapper mapper) {
        final List<Module> modules = ObjectMapper.findModules();

        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new GuavaExtrasModule());
        mapper.registerModule(new CaffeineModule());

        final Module acceleratorModule = modules.stream()
                .filter(module -> "AfterburnerModule" .equals(module.getModuleName()))
                .findFirst()
                .orElse(new BlackbirdModule());

        mapper.registerModule(acceleratorModule);
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
