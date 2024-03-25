package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.Deserializers;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.github.benmanes.caffeine.cache.CaffeineSpec;

import java.io.IOException;

/**
 * A Jackson module that can (de)serialize {@link CaffeineSpec CaffeineSpecs}.
 *
 * @since 2.0
 */
public class CaffeineModule extends Module {
    private static class CaffeineSpecDeserializer extends JsonDeserializer<CaffeineSpec> {
        @Override
        public CaffeineSpec deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            final String text = jp.getText();
            return CaffeineSpec.parse(parseSpec(text));
        }

        private String parseSpec(final String text) {
            if (isSpecDisabled(text)) {
                return "initialCapacity=0,maximumSize=0";
            } else {
                return text;
            }
        }

        private boolean isSpecDisabled(final String text) {
            return "off".equalsIgnoreCase(text) || "disabled".equalsIgnoreCase(text);
        }
    }

    private static class CaffeineSpecSerializer extends JsonSerializer<CaffeineSpec> {
        @Override
        public void serialize(CaffeineSpec value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toParsableString());
        }
    }

    private static class CaffeineDeserializers extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                        DeserializationConfig config,
                                                        BeanDescription beanDesc) throws JsonMappingException {
            if (CaffeineSpec.class.isAssignableFrom(type.getRawClass())) {
                return new CaffeineSpecDeserializer();
            }

            return super.findBeanDeserializer(type, config, beanDesc);
        }
    }

    private static class CaffeineSerializers extends Serializers.Base {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            if (CaffeineSpec.class.isAssignableFrom(type.getRawClass())) {
                return new CaffeineSpecSerializer();
            }

            return super.findSerializer(config, type, beanDesc);
        }
    }

    @Override
    public String getModuleName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new CaffeineDeserializers());
        context.addSerializers(new CaffeineSerializers());
    }
}
