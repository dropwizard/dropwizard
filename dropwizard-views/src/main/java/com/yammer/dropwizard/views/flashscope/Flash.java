package com.yammer.dropwizard.views.flashscope;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class Flash implements Map<String, Object> {

    protected static final ObjectMapper objectMapper = new ObjectMapper();
    protected final Map<String, Object> attributes;

    protected Flash(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean containsKey(Object key) {
        return attributes.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return attributes.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return attributes.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return attributes.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return attributes.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> map) {
        attributes.putAll(map);
    }

    @Override
    public void clear() {
        attributes.clear();
    }

    @Override
    public Set<String> keySet() {
        return attributes.keySet();
    }

    @Override
    public Collection<Object> values() {
        return attributes.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return attributes.entrySet();
    }

    public int size() {
        return attributes.size();
    }

    @Override
    public boolean isEmpty() {
        return attributes.isEmpty();
    }
}
