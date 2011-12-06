package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;

import javax.ws.rs.core.MultivaluedMap;

// TODO: 11/14/11 <coda> -- test OptionalExtractor
// TODO: 11/14/11 <coda> -- document OptionalExtractor

public class OptionalExtractor implements MultivaluedParameterExtractor {
    private final String parameterName;
    private final Optional<String> defaultValue;

    public OptionalExtractor(String parameterName, String defaultValue) {
        this.parameterName = parameterName;
        this.defaultValue = Optional.fromNullable(defaultValue);
    }

    @Override
    public String getName() {
        return parameterName;
    }

    @Override
    public String getDefaultStringValue() {
        return defaultValue.orNull();
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        return Optional.fromNullable(parameters.getFirst(parameterName)).or(defaultValue);
    }
}
