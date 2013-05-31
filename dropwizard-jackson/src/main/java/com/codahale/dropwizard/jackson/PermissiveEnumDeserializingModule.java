package com.codahale.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.google.common.base.CharMatcher;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * A module for deserializing enums that is more permissive than the default.
 * <p/>
 * This desieralizer is more permissive in the following ways:
 * <ul>
 *     <li>Whitespace is permitted but stripped from the input.</li>
 *     <li>Lower-case characters are permitted and automatically translated to upper-case.</li>
 * </ul>
 */
public class PermissiveEnumDeserializingModule extends Module {

    private static class PermissiveEnumDeserializer extends StdScalarDeserializer<Enum<?>> {

        private final Method valueOf;

        protected PermissiveEnumDeserializer(Class<Enum<?>> clazz) {
            super(clazz);

            try {
                valueOf = clazz.getDeclaredMethod("valueOf", String.class);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Class " + clazz.getName() + " is not an enum");
            }
        }

        @Override
        public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt)
                throws IOException, JsonProcessingException {
            String text = jp.getText();
            try {
                return (Enum<?>) valueOf.invoke(
                        null, CharMatcher.WHITESPACE.removeFrom(text).toUpperCase(Locale.ENGLISH));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException("Failed to deserialize enum " + getValueClass().getName() + " from " + text, e);
            }
        }
    }

    private static class PermissiveEnumDerializers extends Deserializers.Base {

        @Override
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
        context.addDeserializers(new PermissiveEnumDerializers());
    }
}
