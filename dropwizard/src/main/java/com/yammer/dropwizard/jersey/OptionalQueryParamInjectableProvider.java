package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;

// TODO: 11/14/11 <coda> -- test OptionalQueryParamInjectableProvider
// TODO: 11/14/11 <coda> -- document OptionalQueryParamInjectableProvider

@Provider
public class OptionalQueryParamInjectableProvider implements InjectableProvider<QueryParam, Parameter> {
    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       QueryParam a,
                                       Parameter c) {
        final String parameterName = c.getSourceName();
        if ((parameterName != null) && !parameterName.isEmpty() &&
                c.getParameterClass().isAssignableFrom(Optional.class)) {
            return new MultivaluedParameterExtractorQueryParamInjectable(
                    new OptionalExtractor(parameterName, c.getDefaultValue()),
                    c.isEncoded()
            );
        }
        return null;
    }
}
