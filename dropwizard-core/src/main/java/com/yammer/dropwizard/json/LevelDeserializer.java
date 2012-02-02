package com.yammer.dropwizard.json;

import org.apache.log4j.Level;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;

import java.io.IOException;

public class LevelDeserializer extends JsonDeserializer<Level> {
    @Override
    public Level deserialize(JsonParser jp,
                             DeserializationContext ctxt) throws IOException {
        return Level.toLevel(jp.getText());
    }
}
