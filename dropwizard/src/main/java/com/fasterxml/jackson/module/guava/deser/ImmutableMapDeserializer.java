package com.fasterxml.jackson.module.guava.deser;

import com.google.common.collect.ImmutableMap;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.type.MapType;

import java.io.IOException;

@JsonCachable
public class ImmutableMapDeserializer extends GuavaMapDeserializer<ImmutableMap<Object,Object>>
{
    public ImmutableMapDeserializer(MapType type, KeyDeserializer keyDeser,
            TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, keyDeser, typeDeser, deser);
    }
    
    @Override
    protected ImmutableMap<Object, Object> _deserializeEntries(JsonParser jp,
            DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        final KeyDeserializer keyDes = _keyDeserializer;
        final JsonDeserializer<?> valueDes = _valueDeserializer;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;

        ImmutableMap.Builder<Object,Object> builder = ImmutableMap.builder();
        for (; jp.getCurrentToken() == JsonToken.FIELD_NAME; jp.nextToken()) {
            // Must point to field name now
            String fieldName = jp.getCurrentName();
            Object key = (keyDes == null) ? fieldName : keyDes.deserializeKey(fieldName, ctxt);
            // And then the value...
            JsonToken t = jp.nextToken();
            // 28-Nov-2010, tatu: Should probably support "ignorable properties" in future...
            Object value;            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            builder.put(key, value);
        }
        return builder.build();
    }

}
