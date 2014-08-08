package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import com.sun.jersey.api.ParamException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.api.representation.Form;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ProviderServices;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.server.impl.model.method.dispatch.FormDispatchProvider;
import com.sun.jersey.server.impl.model.parameter.multivalued.ExtractorContainerException;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor;
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractorFactory;
import com.sun.jersey.server.impl.model.parameter.multivalued.StringReaderFactory;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.FormParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Provider
public class OptionalFormParamInjectableProvider implements InjectableProvider<FormParam, Parameter> {
    private static class FormParamInjectable extends AbstractHttpContextInjectable<Object> {
        private final MultivaluedParameterExtractor extractor;

        private FormParamInjectable(MultivaluedParameterExtractor extractor) {
            this.extractor = extractor;
        }

        @Override
        public Object getValue(HttpContext context) {

            Form form = getCachedForm(context);

            if (form == null) {
                form = getForm(context);
                cacheForm(context, form);
            }

            try {
                return extractor.extract(form);
            } catch (ExtractorContainerException e) {
                throw new ParamException.FormParamException(e.getCause(),
                        extractor.getName(), extractor.getDefaultStringValue());
            }
        }

        private void cacheForm(final HttpContext context, final Form form) {
            context.getProperties().put(FormDispatchProvider.FORM_PROPERTY, form);
        }

        private Form getCachedForm(final HttpContext context) {
            return (Form) context.getProperties().get(FormDispatchProvider.FORM_PROPERTY);
        }

        private HttpRequestContext ensureValidRequest(final HttpRequestContext r) throws IllegalStateException {
            if (r.getMethod().equals("GET")) {
                throw new IllegalStateException(
                        "The @FormParam is utilized when the request method is GET");
            }

            if (!MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, r.getMediaType())) {
                throw new IllegalStateException(
                        "The @FormParam is utilized when the content type of the request entity "
                                + "is not application/x-www-form-urlencoded");
            }
            return r;
        }

        private Form getForm(HttpContext context) {
            final HttpRequestContext r = ensureValidRequest(context.getRequest());
            return r.getFormParameters();
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

    public OptionalFormParamInjectableProvider(@Context ProviderServices services) {
        this.services = services;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       FormParam a,
                                       Parameter c) {
        if (isExtractable(c)) {
            final OptionalExtractor extractor = new OptionalExtractor(getFactory().get(unpack(c)));
            return new FormParamInjectable(extractor);
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
