package com.fasterxml.jackson.module.guava;

import com.fasterxml.jackson.module.guava.ser.OptionalSerializer;
import com.google.common.base.Optional;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.type.JavaType;

public class GuavaSerializers extends Serializers.Base {
    @Override
    public JsonSerializer<?> findSerializer(SerializationConfig config,
                                            JavaType type,
                                            BeanDescription beanDesc,
                                            BeanProperty property) {
        if (Optional.class.isAssignableFrom(type.getRawClass())) {
            return new OptionalSerializer<Object>();
        }
        return super.findSerializer(config, type, beanDesc, property);
    }
}
