package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;

import javax.ws.rs.core.MultivaluedMap;

public class OptionalQueryParamValueFactory extends AbstractContainerRequestValueFactory<Object> {

    private final MultivaluedParameterExtractor<?> extractor;
    private final boolean decode;

    public OptionalQueryParamValueFactory(MultivaluedParameterExtractor<?> extractor, boolean decode) {
        this.extractor = extractor;
        this.decode = decode;
    }

    @Override
    public Object provide() {
        Object value = extractor.extract(getQueryParameters());
        return Optional.fromNullable(value);
    }

    private MultivaluedMap<String, String> getQueryParameters() {
        return getContainerRequest().getUriInfo().getQueryParameters(decode);
    }
}
