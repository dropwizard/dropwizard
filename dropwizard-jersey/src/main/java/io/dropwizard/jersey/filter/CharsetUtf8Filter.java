package io.dropwizard.jersey.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * This class ensures that any HTTP response that includes a Content-Type
 * response header, will also include the UTF-8 character set.
 */
@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class CharsetUtf8Filter implements ContainerResponseFilter {

    private static final String UTF_8 = StandardCharsets.UTF_8.displayName(Locale.ENGLISH);

    @Override
    public void filter(final ContainerRequestContext request,
            final ContainerResponseContext response) throws IOException {

        final MediaType type = response.getMediaType();
        if (type != null && !type.getParameters().containsKey(MediaType.CHARSET_PARAMETER)) {
            final MediaType typeWithCharset = type.withCharset(UTF_8);
            response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, typeWithCharset);
        }
    }
}
