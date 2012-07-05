package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;

import javax.ws.rs.core.MultivaluedMap;

// TODO: 11/14/11 <coda> -- test OptionalExtractor
// TODO: 11/14/11 <coda> -- document OptionalExtractor

public class OptionalExtractor implements MultivaluedParameterExtractor {
    private final MultivaluedParameterExtractor extractor;

    public OptionalExtractor(MultivaluedParameterExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public String getName() {
        return extractor.getName();
    }

    @Override
    public String getDefaultStringValue() {
        return extractor.getDefaultStringValue();
    }

    @Override
    public Object extract(MultivaluedMap<String, String> parameters) {
        return Optional.fromNullable(extractor.extract(parameters));
    }
}
