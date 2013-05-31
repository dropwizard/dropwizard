package com.codahale.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.util.Locale;

/**
 * A module for deserializing enums that is more permissive than the default.
 * <p/>
 * This deserializer is more permissive in the following ways:
 * <ul>
 * <li>Whitespace is permitted but stripped from the input.</li>
 * <li>Lower-case characters are permitted and automatically translated to upper-case.</li>
 * <li>Dashes in the value are converted to underscores.</li>
 * </ul>
 */
public class FuzzyEnumModule extends Module {
    private static class PermissiveEnumDeserializer extends StdScalarDeserializer<Enum<?>> {
        protected PermissiveEnumDeserializer(Class<Enum<?>> clazz) {
            super(clazz);
        }

        @Override
        @SuppressWarnings("unchecked")
        public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Enum<?>[] constants = ((Class<Enum<?>>) getValueClass()).getEnumConstants();
            String text = CharMatcher
                    .WHITESPACE.removeFrom(jp.getText())
                    .replace('-', '_')
                    .toUpperCase(Locale.ENGLISH);

            for (Enum<?> constant : constants) {
                if (constant.name().equals(text)) {
                    return constant;
                }
            }

            throw ctxt.mappingException(getValueClass());
        }
    }

    private static class PermissiveEnumDeserializers extends Deserializers.Base {
        @Override
        @SuppressWarnings("unchecked")
        public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                        DeserializationConfig config,
                                                        BeanDescription desc) throws JsonMappingException {
            return new PermissiveEnumDeserializer((Class<Enum<?>>) type);
        }
    }

    @Override
    public String getModuleName() {
        return "permissive-enums";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(final SetupContext context) {
        context.addDeserializers(new PermissiveEnumDeserializers());
    }
}
