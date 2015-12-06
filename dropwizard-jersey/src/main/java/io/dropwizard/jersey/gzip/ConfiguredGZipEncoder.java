package io.dropwizard.jersey.gzip;

import java.io.IOException;
import java.util.zip.GZIPOutputStream;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.WriterInterceptor;
import javax.ws.rs.ext.WriterInterceptorContext;

/**
 * GZIP encoding support. Writer interceptor that encodes the output  if
 * {@link HttpHeaders#CONTENT_ENCODING Content-Encoding header} value equals
 * to {@code gzip} or {@code x-gzip}.
 *
 * If so configured, it will encode the output even if the  {@code gzip} and {@code x-gzip}
 * {@link HttpHeaders#CONTENT_ENCODING Content-Encoding header} is missing, and insert a value
 * of {@code gzip} for that header.
 *
 */
@Provider
@Priority(Priorities.ENTITY_CODER)
public class ConfiguredGZipEncoder implements WriterInterceptor, ClientRequestFilter {
    private boolean forceEncoding = false;

    public ConfiguredGZipEncoder(boolean forceEncoding) {
        this.forceEncoding = forceEncoding;
    }

    @Override
    public void filter(ClientRequestContext context) throws IOException {
        if (context.hasEntity() && context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING) == null && this.forceEncoding) {
            context.getHeaders().add(HttpHeaders.CONTENT_ENCODING, "gzip");
        }
    }

    @Override
    public final void aroundWriteTo(WriterInterceptorContext context) throws IOException {
        final String contentEncoding = (String) context.getHeaders().getFirst(HttpHeaders.CONTENT_ENCODING);
        if ((contentEncoding != null) &&
                (contentEncoding.equals("gzip") || contentEncoding.equals("x-gzip"))) {
            context.setOutputStream(new GZIPOutputStream(context.getOutputStream()));
        }
        context.proceed();
    }

}
