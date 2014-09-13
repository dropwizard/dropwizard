package io.dropwizard.jersey.guava;

import com.google.common.base.Optional;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor;

import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Map;

public class OptionalCookieParamValueFactory extends AbstractContainerRequestValueFactory<Object> {

    private final MultivaluedParameterExtractor<?> extractor;

    public OptionalCookieParamValueFactory(MultivaluedParameterExtractor<?> extractor) {
        this.extractor = extractor;
    }

    @Override
    public Object provide() {
        final Object value = extractor.extract(getCookies());
        return Optional.fromNullable(value);
    }

    private MultivaluedMap<String, String> getCookies() {
        final Map<String, Cookie> requestCookies = getContainerRequest().getCookies();
        final MultivaluedMap<String, String> cookies = new MultivaluedStringMap(requestCookies.size());

        for (Map.Entry<String, Cookie> e : requestCookies.entrySet()) {
            cookies.putSingle(e.getKey(), e.getValue().getValue());
        }

        return cookies;
    }
}
