package com.yammer.dropwizard.json;

import ch.qos.logback.classic.Level;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.JavaType;

import java.io.IOException;

class LogbackModule extends Module {
    private static class LevelDeserializer extends JsonDeserializer<Level> {
        @Override
        public Level deserialize(JsonParser jp,
                                 DeserializationContext ctxt) throws IOException {
            return Level.toLevel(jp.getText());
        }
    }

    private static class Log4jDeserializers extends Deserializers.Base {
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
        return "log4j";
    }

    @Override
    public Version version() {
        return Version.unknownVersion();
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addDeserializers(new Log4jDeserializers());
    }
}
