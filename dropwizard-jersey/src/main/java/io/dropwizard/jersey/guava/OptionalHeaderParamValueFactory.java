package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;

import javax.ws.rs.core.MultivaluedMap;

public class OptionalHeaderParamValueFactory extends AbstractContainerRequestValueFactory<Object> {

    private final MultivaluedParameterExtractor<?> extractor;

    public OptionalHeaderParamValueFactory(MultivaluedParameterExtractor<?> extractor) {
        this.extractor = extractor;
    }

    @Override
    public Object provide() {
        Object value = extractor.extract(getRequestHeaders());
        return Optional.fromNullable(value);
    }

    private MultivaluedMap<String, String> getRequestHeaders() {
        return getContainerRequest().getRequestHeaders();
    }
}
