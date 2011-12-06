package com.fasterxml.jackson.module.guava.deser;

import com.google.common.collect.HashMultiset;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.annotate.JsonCachable;
import org.codehaus.jackson.map.type.CollectionType;

import java.io.IOException;

@JsonCachable
public class HashMultisetDeserializer  extends GuavaCollectionDeserializer<HashMultiset<Object>>
{
    public HashMultisetDeserializer(CollectionType type, TypeDeserializer typeDeser, JsonDeserializer<?> deser)
    {
        super(type, typeDeser, deser);
    }

    @Override
    protected HashMultiset<Object> _deserializeContents(JsonParser jp, DeserializationContext ctxt)
        throws IOException, JsonProcessingException
    {
        JsonDeserializer<?> valueDes = _valueDeserializer;
        JsonToken t;
        final TypeDeserializer typeDeser = _typeDeserializerForValue;
        HashMultiset<Object> set = HashMultiset.create();

        while ((t = jp.nextToken()) != JsonToken.END_ARRAY) {
            Object value;
            
            if (t == JsonToken.VALUE_NULL) {
                value = null;
            } else if (typeDeser == null) {
                value = valueDes.deserialize(jp, ctxt);
            } else {
                value = valueDes.deserializeWithType(jp, ctxt, typeDeser);
            }
            set.add(value);
        }
        return set;
    }
}
