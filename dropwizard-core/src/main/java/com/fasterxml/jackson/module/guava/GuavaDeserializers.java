package com.fasterxml.jackson.module.guava;

import com.fasterxml.jackson.module.guava.deser.*;
import com.google.common.base.Optional;
import com.google.common.collect.*;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.type.JavaType;

/**
 * Custom deserializers module offers.
 * 
 * @author tsaloranta
 */
public class GuavaDeserializers
    extends Deserializers.Base
{
    @Override
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
                                                    DeserializationConfig config,
                                                    DeserializerProvider provider,
                                                    BeanDescription beanDesc,
                                                    BeanProperty property) throws JsonMappingException {
        if (Optional.class.isAssignableFrom(type.getRawClass())) {
            final JavaType elementType = type.containedType(0);
            return new OptionalDeserializer<Object>(
                    provider.findTypedValueDeserializer(config, elementType, property));
        }
        return super.findBeanDeserializer(type, config, provider, beanDesc, property);
    }

    /**
     * Concrete implementation class to use for properties declared as
     * {@link Multiset}s.
     * Defaults to using 
     */
//    protected Class<? extends Multiset<?>> _cfgDefaultMultiset;

//    protected Class<? extends Multimap<?>> _cfgDefaultMultimap;
    
    /*
     * No bean types to support yet; may need to add?
     */
    /*
    public JsonDeserializer<?> findBeanDeserializer(JavaType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc) {
        return null;
    }
    */

    @Override
    public JsonDeserializer<?> findCollectionDeserializer(CollectionType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException
    {
        Class<?> raw = type.getRawClass();

        // Multi-xxx collections?
        if (Multiset.class.isAssignableFrom(raw)) {
            // Quite a few variations...
            if (LinkedHashMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (HashMultiset.class.isAssignableFrom(raw)) {
                return new HashMultisetDeserializer(type, elementTypeDeser,
                        _verifyElementDeserializer(elementDeser, type, config, provider));
            }
            if (ImmutableMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (EnumMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (TreeMultiset.class.isAssignableFrom(raw)) {
                // !!! TODO
            }

            // TODO: make configurable (for now just default blindly)
            return new HashMultisetDeserializer(type, elementTypeDeser,
                    _verifyElementDeserializer(elementDeser, type, config, provider));
        }
        
        // ImmutableXxx types?
        if (ImmutableCollection.class.isAssignableFrom(raw)) {
            if (ImmutableList.class.isAssignableFrom(raw)) {
                return new ImmutableListDeserializer(type, elementTypeDeser,
                        _verifyElementDeserializer(elementDeser, type, config, provider));
            }
            if (ImmutableSet.class.isAssignableFrom(raw)) {
                // sorted one?
                if (ImmutableSortedSet.class.isAssignableFrom(raw)) {
                    /* 28-Nov-2010, tatu: With some more work would be able to use other things
                     *   than natural ordering; but that'll have to do for now...
                     */
                    Class<?> elemType = type.getContentType().getRawClass();
                    if (!Comparable.class.isAssignableFrom(elemType)) {
                        throw new IllegalArgumentException("Can not handle ImmutableSortedSet with elements that are not Comparable<?> ("
                                +raw.getName()+")");
                    }
                    return new ImmutableSortedSetDeserializer(type, elementTypeDeser,
                            _verifyElementDeserializer(elementDeser, type, config, provider));
                }
                // nah, just regular one
                return new ImmutableSetDeserializer(type, elementTypeDeser,
                        _verifyElementDeserializer(elementDeser, type, config, provider));
            }
        }
        return null;
    }

    /**
     * A few Map types to support.
     */
    @Override
    public JsonDeserializer<?> findMapDeserializer(MapType type,
            DeserializationConfig config, DeserializerProvider provider,
            BeanDescription beanDesc, BeanProperty property, KeyDeserializer keyDeser,
            TypeDeserializer elementTypeDeser, JsonDeserializer<?> elementDeser)
        throws JsonMappingException
    {
        Class<?> raw = type.getRawClass();
        // ImmutableXxxMap types?
        if (ImmutableMap.class.isAssignableFrom(raw)) {
            if (ImmutableSortedMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (ImmutableBiMap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            // Otherwise, plain old ImmutableMap...
            return new ImmutableMapDeserializer(type, keyDeser, elementTypeDeser,
                    _verifyElementDeserializer(elementDeser, type, config, provider));
        }
        // Multimaps?
        if (Multimap.class.isAssignableFrom(raw)) {
            if (ListMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (SetMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
            if (SortedSetMultimap.class.isAssignableFrom(raw)) {
                // !!! TODO
            }
        }
        return null;
    }

    /*
    /**********************************************************************
    /* Helper methods
    /**********************************************************************
     */

    /**
     * Helper method used to ensure that we have a deserializer for elements
     * of collection being deserialized.
     */
    JsonDeserializer<?> _verifyElementDeserializer(JsonDeserializer<?> deser,
            JavaType type,
            DeserializationConfig config, DeserializerProvider provider)
        throws JsonMappingException
    {
        if (deser == null) {
            // 'null' -> collections have no referring fields
            deser = provider.findValueDeserializer(config, type.getContentType(), null);
        }
        return deser;
    }
}
