package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.common.spi.AccessEvent;
import ch.qos.logback.access.common.spi.ServerAdapter;
import ch.qos.logback.core.Context;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class SafeRequestParameterConverterTest {

    private final SafeRequestParameterConverter safeRequestParameterConverter = new SafeRequestParameterConverter();
    private final HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
    private AccessEvent accessEvent;

    @BeforeEach
    void setUp() throws Exception {
        accessEvent = new AccessEvent(Mockito.mock(Context.class), httpServletRequest, Mockito.mock(HttpServletResponse.class),
            Mockito.mock(ServerAdapter.class));

        safeRequestParameterConverter.setOptionList(Collections.singletonList("name"));
        safeRequestParameterConverter.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        safeRequestParameterConverter.stop();
    }

    @Test
    void testConvertOneParameter() throws Exception {
        Mockito.when(httpServletRequest.getParameterValues("name")).thenReturn(new String[]{"Alice"});
        Mockito.when(httpServletRequest.getParameterNames())
                .thenReturn(Collections.enumeration(Collections.singleton("name")));

        // Invoked by AccessEvent#prepareForDeferredProcessing
        accessEvent.buildRequestParameterMap();
        // Jetty recycled the request
        Mockito.reset(httpServletRequest);

        String value = safeRequestParameterConverter.convert(accessEvent);
        assertThat(value).isEqualTo("Alice");
    }

    @Test
    void testConvertSeveralParameters() throws Exception {
        Mockito.when(httpServletRequest.getParameterValues("name")).thenReturn(new String[]{"Alice", "Bob"});
        Mockito.when(httpServletRequest.getParameterNames())
                .thenReturn(Collections.enumeration(Collections.singleton("name")));

        // Invoked by AccessEvent#prepareForDeferredProcessing
        accessEvent.buildRequestParameterMap();
        // Jetty recycled the request
        Mockito.reset(httpServletRequest);

        final String value = safeRequestParameterConverter.convert(accessEvent);
        assertThat(value).isEqualTo("[Alice, Bob]");
    }
}
