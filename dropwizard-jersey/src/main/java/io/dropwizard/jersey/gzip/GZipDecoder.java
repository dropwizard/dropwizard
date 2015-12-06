package io.dropwizard.jersey.gzip;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

/**
 * GZIP encoding support. Reader interceptor that decodes the input  if
 * {@link HttpHeaders#CONTENT_ENCODING Content-Encoding header} value equals
 * to {@code gzip} or {@code x-gzip}.
 *
 * We're using this instead of Jersey's built in {@link org.glassfish.jersey.message.GZipEncoder}
 * because that unconditionally encodes on writing, whereas dropwizard-client
 * needs the encoding to be configurable. See {@link ConfiguredGZipEncoder}
 *
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class GZipDecoder implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException {
        if (!context.getHeaders().containsKey(HttpHeaders.ACCEPT_ENCODING)) {
            context.getHeaders().add(HttpHeaders.ACCEPT_ENCODING, "gzip");
        }

        final String contentEncoding = context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if (contentEncoding != null &&
                (contentEncoding.equals("gzip") || contentEncoding.equals("x-gzip"))) {
            context.setInputStream(new GZIPInputStream(context.getInputStream()));
        }
        return context.proceed();
    }

}
