package com.yammer.dropwizard.jersey;

import com.google.common.base.Optional;
import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.model.parameter.multivalued.ExtractorContainerException;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorFactory;
import com.sun.jersey.server.impl.model.parameter.multivalued.StringReaderFactory;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Provider
public class OptionalQueryParamInjectableProvider implements InjectableProvider<QueryParam, Parameter> {
    private static class QueryParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;
        private final boolean decode;

        private QueryParamInjectable(MultivaluedParameterExtractor extractor,
                                     boolean decode) {
            this.extractor = extractor;
            this.decode = decode;
        }

        @Override
        public Object getValue(HttpContext c) {
            try {
                return extractor.extract(c.getUriInfo().getQueryParameters(decode));
            } catch (ExtractorContainerException e) {
                throw new ParamException.QueryParamException(e.getCause(),
                                                             extractor.getName(),
                                                             extractor.getDefaultStringValue());
            }
        }
    }

    private static class OptionalExtractor implements MultivaluedParameterExtractor {
        private final MultivaluedParameterExtractor extractor;

        private OptionalExtractor(MultivaluedParameterExtractor extractor) {
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

    private final ProviderServices services;
    private MultivaluedParameterExtractorFactory factory;

    public OptionalQueryParamInjectableProvider(@Context ProviderServices services) {
        this.services = services;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       QueryParam a,
                                       Parameter c) {
        if (isExtractable(c)) {
            final OptionalExtractor extractor = new OptionalExtractor(getFactory().get(unpack(c)));
            return new QueryParamInjectable(extractor, !c.isEncoded());
        }
        return null;
    }

    private boolean isExtractable(Parameter param) {
        return (param.getSourceName() != null) && !param.getSourceName().isEmpty() &&
                param.getParameterClass().isAssignableFrom(Optional.class) &&
                (param.getParameterType() instanceof ParameterizedType);
    }

    private Parameter unpack(Parameter param) {
        final Type typeParameter = ((ParameterizedType) param.getParameterType()).getActualTypeArguments()[0];
        return new Parameter(param.getAnnotations(),
                             param.getAnnotation(),
                             param.getSource(),
                             param.getSourceName(),
                             typeParameter,
                             (Class<?>) typeParameter,
                             param.isEncoded(),
                             param.getDefaultValue());
    }

    private MultivaluedParameterExtractorFactory getFactory() {
        if (factory == null) {
            final StringReaderFactory stringReaderFactory = new StringReaderFactory();
            stringReaderFactory.init(services);

            this.factory = new MultivaluedParameterExtractorFactory(stringReaderFactory);
        }

        return factory;
    }
}
