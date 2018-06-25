package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SafeRequestParameterConverterTest {

    private final SafeRequestParameterConverter safeRequestParameterConverter = new SafeRequestParameterConverter();
    private final HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
    private AccessEvent accessEvent;

    @Before
    public void setUp() throws Exception {
        accessEvent = new AccessEvent(httpServletRequest, Mockito.mock(HttpServletResponse.class),
            Mockito.mock(ServerAdapter.class));

        safeRequestParameterConverter.setOptionList(Collections.singletonList("name"));
        safeRequestParameterConverter.start();
    }

    @After
    public void tearDown() throws Exception {
        safeRequestParameterConverter.stop();
    }

    @Test
    public void testConvertOneParameter() throws Exception {
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
    public void testConvertSeveralParameters() throws Exception {
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
