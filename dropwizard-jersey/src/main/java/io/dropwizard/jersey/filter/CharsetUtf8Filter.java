package io.dropwizard.jersey.filter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

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
