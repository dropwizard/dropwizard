package com.yammer.dropwizard.json;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.Deserializers;

import java.io.IOException;

class LogbackModule extends Module {
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
                                                        BeanDescription beanDesc) throws JsonMappingException {
            if (Level.class.isAssignableFrom(type.getRawClass())) {
                return new LevelDeserializer();
            }
            return super.findBeanDeserializer(type, config, beanDesc);
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
