package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.deser.std.EnumDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * A module for deserializing enums that is more permissive than the default.
 * <p/>
 * This deserializer is more permissive in the following ways:
 * <ul>
 * <li>Whitespace is permitted but stripped from the input.</li>
 * <li>Dashes and periods in the value are converted to underscores.</li>
 * <li>Matching against the enum values is case insensitive.</li>
 * </ul>
 */
public class FuzzyEnumModule extends Module {
    private static class PermissiveEnumDeserializer extends StdScalarDeserializer<Enum<?>> {
        private static final long serialVersionUID = 1L;

        private final Enum<?>[] constants;
        private final List<String> acceptedValues;

        @SuppressWarnings("unchecked")
        protected PermissiveEnumDeserializer(Class<Enum<?>> clazz) {
            super(clazz);
            this.constants = ((Class<Enum<?>>) handledType()).getEnumConstants();
            this.acceptedValues = Lists.newArrayList();
            for (Enum<?> constant : constants) {
                acceptedValues.add(constant.name());
            }
        }

        @Override

        public Enum<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            final String text = CharMatcher.WHITESPACE
                    .removeFrom(jp.getText())
                    .replace('-', '_')
                    .replace('.', '_');
            for (Enum<?> constant : constants) {
                if (constant.name().equalsIgnoreCase(text)) {
                    return constant;
                }
            }

            throw ctxt.mappingException(text + " was not one of " + acceptedValues);
        }
    }

    private static class PermissiveEnumDeserializers extends Deserializers.Base {
        @Override
        @SuppressWarnings("unchecked")
        public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                                                        DeserializationConfig config,
                                                        BeanDescription desc) throws JsonMappingException {
            // If there is a JsonCreator annotation we should use that instead of the PermissiveEnumDeserializer
            final Collection<AnnotatedMethod> factoryMethods = desc.getFactoryMethods();
            if (factoryMethods != null) {
                for (AnnotatedMethod am : factoryMethods) {
                    final JsonCreator creator = am.getAnnotation(JsonCreator.class);
                    if (creator != null) {
                        return EnumDeserializer.deserializerForCreator(config, type, am);
                    }
                }
            }

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
