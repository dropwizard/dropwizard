package com.yammer.dropwizard.json;

import ch.qos.logback.classic.Level;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;

class LogbackModule extends Module {
    @JsonCachable
    private static class LevelDeserializer extends JsonDeserializer<Level> {
        @Override
        public Level deserialize(JsonParser jp,
                                 DeserializationContext ctxt) throws IOException {

            final String text = jp.getText();

            if ("false".equalsIgnoreCase(text)) {
                return Level.OFF;
            }

            if ("true".equalsIgnoreCase(text)) {
                return Level.ALL;
            }

            return Level.toLevel(text, Level.INFO);
        }
    }

    private static class LogbackDeserializers extends Deserializers.Base {
        @Override
        public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                        DeserializationConfig config,
                                                        DeserializerProvider provider,
                                                        BeanDescription beanDesc,
                                                        BeanProperty property) throws JsonMappingException {
            if (Level.class.isAssignableFrom(type.getRawClass())) {
                return new LevelDeserializer();
            }

            return super.findBeanDeserializer(type, config, provider, beanDesc, property);
        }
    }

    @Override
    public String getModuleName() {
        return "LogbackModule";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new LogbackDeserializers());
    }
}
