package io.dropwizard.views;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

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
import java.util.ServiceLoader;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML })
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {
    private static final String MISSING_TEMPLATE_MSG =
            "<html>" +
                "<head><title>Missing Template</title></head>" +
                "<body><h1>Missing Template</h1><p>Template \"{0}\" not found.</p></body>" +
            "</html>";

    @Context
    @SuppressWarnings("UnusedDeclaration")
    private HttpHeaders headers;

    private final Iterable<ViewRenderer> renderers;
    private final MetricRegistry metricRegistry;

    @Deprecated
    public ViewMessageBodyWriter(MetricRegistry metricRegistry) {
        this(metricRegistry, ServiceLoader.load(ViewRenderer.class));
    }

    public ViewMessageBodyWriter(MetricRegistry metricRegistry, Iterable<ViewRenderer> viewRenderers) {
        this.metricRegistry = metricRegistry;
        this.renderers = viewRenderers;
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
                        OutputStream entityStream) throws IOException {
        final Timer.Context context = metricRegistry.timer(name(t.getClass(), "rendering")).time();
        try {
            for (ViewRenderer renderer : renderers) {
                if (renderer.isRenderable(t)) {
                    renderer.render(t, detectLocale(headers), entityStream);
                    return;
                }
            }
            throw new ViewRenderException("Unable to find a renderer for " + t.getTemplateName());
        } catch (FileNotFoundException e) {
            final String msg = MessageFormat.format(MISSING_TEMPLATE_MSG, t.getTemplateName());
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
