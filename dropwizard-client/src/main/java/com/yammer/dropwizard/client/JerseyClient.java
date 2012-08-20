package com.yammer.dropwizard.client;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;

import javax.ws.rs.core.MediaType;
import java.net.URI;

public class JerseyClient extends ApacheHttpClient4 {
    public JerseyClient(ApacheHttpClient4Handler root, ClientConfig config) {
        super(root, config);
    }

    public <T> T get(URI uri, MediaType acceptedMediaType, Class<T> klass) {
        return resource(uri).accept(acceptedMediaType).get(klass);
    }

    public <T> T get(URI uri, MediaType acceptedMediaType, GenericType<T> klass) {
        return resource(uri).accept(acceptedMediaType).get(klass);
    }

    public <T> T put(URI uri, MediaType contentType, Object entity, Class<T> returnType) {
        return resource(uri).type(contentType).put(returnType, entity);
    }

    public <T> T put(URI uri, MediaType contentType, Object entity, GenericType<T> returnType) {
        return resource(uri).type(contentType).put(returnType, entity);
    }

    public <T> T post(URI uri, MediaType contentType, Object entity, Class<T> returnType) {
        return resource(uri).type(contentType).post(returnType, entity);
    }

    public <T> T post(URI uri, MediaType contentType, Object entity, GenericType<T> returnType) {
        return resource(uri).type(contentType).post(returnType, entity);
    }

    public <T> T delete(URI uri, Class<T> klass) {
        return resource(uri).delete(klass);
    }

    public <T> T delete(URI uri, GenericType<T> klass) {
        return resource(uri).delete(klass);
    }
}
