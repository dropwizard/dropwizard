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

        //These values are only provided if the @JsonCreator annotation is provided
        private DeserializationConfig config;
        private AnnotatedMethod am;

        @SuppressWarnings("unchecked")
        protected PermissiveEnumDeserializer(Class<Enum<?>> clazz) {
            super(clazz);
            this.constants = ((Class<Enum<?>>) handledType()).getEnumConstants();
            this.acceptedValues = Lists.newArrayList();
            for (Enum<?> constant : constants) {
                acceptedValues.add(constant.name());
            }
        }

        @SuppressWarnings("unchecked")
        protected PermissiveEnumDeserializer(Class<?> type,
                DeserializationConfig config,
                AnnotatedMethod am) {
            super(type);
            this.constants = ((Class<Enum<?>>) handledType()).getEnumConstants();
            this.acceptedValues = Lists.newArrayList();
            for (Enum<?> constant : constants) {
                acceptedValues.add(constant.name());
            }
            this.config = config;
            this.am = am;
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

            //In some cases there are certain enums that don't follow the same patter across an enterprise.  So this
            //means that you have a mix of enums that use toString(), some use @JsonCreator, and some just use the
            //standard constant name().  This block handles finding the proper enum by toString()
            String toStringText = jp.getText()
                    .replace('-', '_')
                    .replace('.', '_');
            for (Enum<?> constant : constants) {
                if (constant.toString().equalsIgnoreCase(toStringText)) {
                    return constant;
                }
            }

            //If the config was provided then we're assuming that the class we're deserializing had @JsonCreator
            //annotation so we'll try to deserialize with that if the we weren't able to find the enum yet.
            if (config != null) {
                return (Enum<?>) EnumDeserializer.deserializerForCreator(config, this.handledType(), am).deserialize(jp,
                        ctxt);
            } else {
                throw ctxt.mappingException(text + " was not one of " + acceptedValues);
            }
        }
    }

    private static class PermissiveEnumDeserializers extends Deserializers.Base {

        @Override
        @SuppressWarnings("unchecked")
        public JsonDeserializer<?> findEnumDeserializer(Class<?> type,
                DeserializationConfig config,
                BeanDescription desc) throws JsonMappingException {
            // If the user configured to use `toString` method to deserialize enums
            if (config.hasDeserializationFeatures(
                    DeserializationFeature.READ_ENUMS_USING_TO_STRING.getMask())) {
                return null;
            }

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
