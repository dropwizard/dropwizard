package io.dropwizard.views;

import com.codahale.metrics.MetricRegistry;
import org.glassfish.jersey.message.internal.HeaderValueException;
import org.glassfish.jersey.server.ContainerRequest;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import java.util.Collections;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

public class ViewMessageBodyWriterTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();
    @Mock
    public ContainerRequest headers;
    @Mock
    public MetricRegistry metricRegistry;

    @Test
    public void detectLocaleShouldHandleBadlyFormedHeader() {
        when(headers.getAcceptableLanguages()).thenThrow(HeaderValueException.class);

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());

        assertThatExceptionOfType(WebApplicationException.class).isThrownBy(() -> {
            writer.detectLocale(headers);
        });
    }

    @Test
    public void detectLocaleShouldReturnDefaultLocaleWhenHeaderNotSpecified() {
        // We call the real methods to make sure that 'getAcceptableLanguages' returns a locale with a wildcard
        // (which is their default value). This also validates that 'detectLocale' skips wildcard languages.
        when(headers.getAcceptableLanguages()).thenCallRealMethod();
        when(headers.getQualifiedAcceptableLanguages()).thenCallRealMethod();
        when(headers.getHeaderString(HttpHeaders.ACCEPT_LANGUAGE)).thenReturn(null);

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());
        final Locale result = writer.detectLocale(headers);

        assertThat(result).isSameAs(Locale.getDefault());
    }

    @Test
    public void detectLocaleShouldReturnCorrectLocale() {
        final Locale fakeLocale = new Locale("en-US");
        when(headers.getAcceptableLanguages()).thenReturn(Collections.singletonList(fakeLocale));

        final ViewMessageBodyWriter writer = new ViewMessageBodyWriter(metricRegistry, Collections.emptyList());
        final Locale result = writer.detectLocale(headers);

        assertThat(result).isSameAs(fakeLocale);
    }
}
