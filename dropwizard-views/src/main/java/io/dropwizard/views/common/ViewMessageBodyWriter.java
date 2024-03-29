package io.dropwizard.views.common;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.message.internal.HeaderValueException;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Locale;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.Objects.requireNonNull;

@Provider
@Produces({MediaType.TEXT_HTML, MediaType.APPLICATION_XHTML_XML})
public class ViewMessageBodyWriter implements MessageBodyWriter<View> {

    @Context
    @Nullable
    private HttpHeaders headers;

    void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    private final Iterable<ViewRenderer> renderers;
    private final MetricRegistry metricRegistry;

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
                    renderer.render(t, detectLocale(requireNonNull(headers)), entityStream);
                    return;
                }
            }
            throw new ViewRenderException("Unable to find a renderer for " + t.getTemplateName());
        } catch (ViewRenderException e) {
            throw new WebApplicationException(e);
        } finally {
            context.stop();
        }
    }

    protected Locale detectLocale(HttpHeaders headers) {
        final List<Locale> languages;
        try {
            languages = headers.getAcceptableLanguages();
        } catch (HeaderValueException e) {
            throw new WebApplicationException(e.getMessage(), Response.Status.BAD_REQUEST);
        }

        for (Locale locale : languages) {
            if (!locale.toString().contains("*")) { // Freemarker doesn't do wildcards well
                return locale;
            }
        }
        return Locale.getDefault();
    }

    Iterable<ViewRenderer> getRenderers() {
        return renderers;
    }
}
