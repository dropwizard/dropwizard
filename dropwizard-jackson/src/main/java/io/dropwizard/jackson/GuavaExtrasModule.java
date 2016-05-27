package io.dropwizard.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import com.google.common.cache.CacheBuilderSpec;

import java.io.IOException;

public class GuavaExtrasModule extends Module {
    private static class CacheBuilderSpecDeserializer extends JsonDeserializer<CacheBuilderSpec> {
        @Override
        public CacheBuilderSpec deserialize(JsonParser jp,
                                            DeserializationContext ctxt) throws IOException {
            final String text = jp.getText();
            if ("off".equalsIgnoreCase(text) || "disabled".equalsIgnoreCase(text)) {
                return CacheBuilderSpec.disableCaching();
            }
            return CacheBuilderSpec.parse(text);
        }
    }

    private static class CacheBuilderSpecSerializer extends JsonSerializer<CacheBuilderSpec> {
        @Override
        public void serialize(CacheBuilderSpec value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeString(value.toParsableString());
        }
    }

    private static class GuavaExtrasDeserializers extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                        DeserializationConfig config,
                                                        BeanDescription beanDesc) throws JsonMappingException {
            if (CacheBuilderSpec.class.isAssignableFrom(type.getRawClass())) {
                return new CacheBuilderSpecDeserializer();
            }

            return super.findBeanDeserializer(type, config, beanDesc);
        }
    }

    private static class GuavaExtrasSerializers extends Serializers.Base {
        @Override
        public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
            if (CacheBuilderSpec.class.isAssignableFrom(type.getRawClass())) {
                return new CacheBuilderSpecSerializer();
            }

            return super.findSerializer(config, type, beanDesc);
        }
    }

    @Override
    public String getModuleName() {
        return "guava-extras";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new GuavaExtrasDeserializers());
        context.addSerializers(new GuavaExtrasSerializers());
    }
}
