package com.yammer.dropwizard.json;

import com.google.common.cache.CacheBuilderSpec;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;

public class GuavaExtrasModule extends Module {
    @JsonCachable
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

    private static class GuavaExtrasDeserializers extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                        DeserializationConfig config,
                                                        DeserializerProvider provider,
                                                        BeanDescription beanDesc,
                                                        BeanProperty property) throws JsonMappingException {
            if (CacheBuilderSpec.class.isAssignableFrom(type.getRawClass())) {
                return new CacheBuilderSpecDeserializer();
            }
            return super.findBeanDeserializer(type, config, provider, beanDesc, property);
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
    }
}
