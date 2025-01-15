package io.dropwizard.views.common;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import org.glassfish.jersey.message.internal.HeaderValueException;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ViewMessageBodyWriterTest {

    public ContainerRequest headers = mock(ContainerRequest.class);
    public MetricRegistry metricRegistry = mock(MetricRegistry.class);
    public View view = mock(View.class);
    public OutputStream stream = mock(OutputStream.class);
    public Timer timer = mock(Timer.class);
    public Timer.Context timerContext = mock(Timer.Context.class);

    @Test
    void writeToShouldUseValidRenderer() throws IOException {
        final ViewRenderer renderable = mock(ViewRenderer.class);
        final ViewRenderer nonRenderable = mock(ViewRenderer.class);
        final Locale locale = new Locale("en-US");

        when(metricRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        when(renderable.isRenderable(view)).thenReturn(true);
        when(nonRenderable.isRenderable(view)).thenReturn(false);

        final ViewMessageBodyWriter writer = spy(new ViewMessageBodyWriter(metricRegistry, Arrays.asList(nonRenderable, renderable)));
        doReturn(locale).when(writer).detectLocale(any());
        writer.setHeaders(mock(HttpHeaders.class));

        writer.writeTo(view, Class.class, Class.class, new Annotation[]{}, new MediaType(),
            new MultivaluedHashMap<>(), stream);

        verify(nonRenderable).isRenderable(view);
        verifyNoMoreInteractions(nonRenderable);
        verify(renderable).isRenderable(view);
        verify(renderable).render(view, locale, stream);
        verify(timerContext).stop();
    }

    @Test
    void writeToShouldThrowWhenNoValidRendererFound() {
        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());

        when(metricRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> writer.writeTo(view, Class.class, Class.class, new Annotation[]{}, new MediaType(), new MultivaluedHashMap<>(), stream)).withCauseExactlyInstanceOf(ViewRenderException.class);

        verify(timerContext).stop();
    }

    @Test
    void writeToShouldHandleViewRenderingExceptions() throws IOException {
        final ViewRenderer renderer = mock(ViewRenderer.class);
        final Locale locale = new Locale("en-US");
        final ViewRenderException exception = new ViewRenderException("oops");

        when(metricRegistry.timer(anyString())).thenReturn(timer);
        when(timer.time()).thenReturn(timerContext);

        when(renderer.isRenderable(view)).thenReturn(true);
        doThrow(exception).when(renderer).render(view, locale, stream);

        final ViewMessageBodyWriter writer = spy(new ViewMessageBodyWriter(metricRegistry, Collections.singletonList(renderer)));
        doReturn(locale).when(writer).detectLocale(any());
        writer.setHeaders(mock(HttpHeaders.class));

        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> writer.writeTo(view, Class.class, Class.class, new Annotation[]{}, new MediaType(), new MultivaluedHashMap<>(), stream))
            .withCause(exception);

        verify(timerContext).stop();
    }

    @Test
    void detectLocaleShouldHandleBadlyFormedHeader() {
        when(headers.getAcceptableLanguages()).thenThrow(HeaderValueException.class);

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());

        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> writer.detectLocale(headers));
    }

    @Test
    void detectLocaleShouldReturnDefaultLocaleWhenHeaderNotSpecified() {
        // We call the real methods to make sure that 'getAcceptableLanguages' returns a locale with a wildcard
        // (which is their default value). This also validates that 'detectLocale' skips wildcard languages.
        when(headers.getAcceptableLanguages()).thenCallRealMethod();
        when(headers.getQualifiedAcceptableLanguages()).thenCallRealMethod();
        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn(null);

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());

        assertThat(writer.detectLocale(headers))
            .isSameAs(Locale.getDefault());
    }

    @Test
    void detectLocaleShouldReturnCorrectLocale() {
        final Locale fakeLocale = new Locale("en-US");
        when(headers.getAcceptableLanguages()).thenReturn(Collections.singletonList(fakeLocale));

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());

        assertThat(writer.detectLocale(headers))
            .isSameAs(fakeLocale);
    }
}
