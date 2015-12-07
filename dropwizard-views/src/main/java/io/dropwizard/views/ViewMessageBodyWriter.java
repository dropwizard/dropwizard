package io.dropwizard.views;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import static com.codahale.metrics.MetricRegistry.name;

@Provider
@Produces({ MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML })
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {
    private static final Logger logger = LoggerFactory.getLogger(MessageBodyWriter.class);
    public static final String TEMPLATE_ERROR_MSG =
            "<html>" +
                "<head><title>Template Error</title></head>" +
                "<body><h1>Template Error</h1><p>Something went wrong rendering the page</p></body>" +
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
        } catch (Exception e) {
            logger.debug("Template Error", e);
            throw new WebApplicationException(Response.serverError()
                                                      .type(MediaType.TEXT_HTML_TYPE)
                                                      .entity(TEMPLATE_ERROR_MSG)
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
