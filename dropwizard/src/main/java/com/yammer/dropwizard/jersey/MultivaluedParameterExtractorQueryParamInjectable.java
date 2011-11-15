package com.yammer.dropwizard.jersey;

import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.model.parameter.multivalued.ExtractorContainerException;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;

// TODO: 11/14/11 <coda> -- test MultivaluedParameterExtractorQueryParamInjectable
// TODO: 11/14/11 <coda> -- document MultivaluedParameterExtractorQueryParamInjectable

public class MultivaluedParameterExtractorQueryParamInjectable extends AbstractHttpContextInjectable<Object> {
    private final MultivaluedParameterExtractor extractor;
    private final boolean isEncoded;

    public MultivaluedParameterExtractorQueryParamInjectable(MultivaluedParameterExtractor extractor,
                                                             boolean encoded) {
        this.extractor = extractor;
        isEncoded = encoded;
    }

    @Override
    public Object getValue(HttpContext c) {
        try {
            return extractor.extract(c.getUriInfo().getQueryParameters(isEncoded));
        } catch (ExtractorContainerException e) {
            throw new ParamException.QueryParamException(e.getCause(),
                                                         extractor.getName(),
                                                         extractor.getDefaultStringValue());
        }
    }
}
