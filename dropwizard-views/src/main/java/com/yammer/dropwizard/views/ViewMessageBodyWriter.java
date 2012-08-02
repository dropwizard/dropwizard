package com.yammer.dropwizard.views;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.spi.service.ServiceFinder;
import com.yammer.metrics.core.TimerContext;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;

@Provider
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {
    private static final String MISSING_TEMPLATE_MSG =
            "<html>" +
                "<head><title>Missing Template</title></head>" +
                "<body><h1>Missing Template</h1><p>{0}</p></body>" +
            "</html>";

    @Context
    @SuppressWarnings("FieldMayBeFinal")
    private HttpHeaders headers;

    private final ImmutableList<ViewRenderer> renderers;

    @SuppressWarnings("UnusedDeclaration")
    public ViewMessageBodyWriter() {
        this(null);
    }

    @VisibleForTesting
    public ViewMessageBodyWriter(HttpHeaders headers) {
        this.headers = headers;
        this.renderers = ImmutableList.copyOf(ServiceFinder.find(ViewRenderer.class));
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return View.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(View t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(View t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        final TimerContext context = t.getRenderingTimer().time();
        try {
            for (ViewRenderer renderer : renderers) {
                if (renderer.isRenderable(t)) {
                    renderer.render(t, detectLocale(headers), entityStream);
                    return;
                }
            }
            throw new ViewRenderException("Unable to find a renderer for " + t.getTemplateName());
        } catch (FileNotFoundException e) {
            final String msg = MessageFormat.format(MISSING_TEMPLATE_MSG, e.getMessage());
            throw new WebApplicationException(Response.serverError()
                                                      .type(MediaType.TEXT_HTML_TYPE)
                                                      .entity(msg)
                                                      .build());
        } finally {
            context.stop();
        }
    }

    private Locale detectLocale(HttpHeaders headers) {
        final List<Locale> languages = headers.getAcceptableLanguages();
        for (Locale locale : languages) {
            if (!locale.toString().contains("*")) { // Freemarker doesn't do wildcards well
                return locale;
            }
        }
        return Locale.getDefault();
    }
}
