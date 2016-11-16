package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Vector;

import static org.assertj.core.api.Assertions.assertThat;

public class SafeRequestParameterConverterTest {

    private final SafeRequestParameterConverter safeRequestParameterConverter = new SafeRequestParameterConverter();
    private final HttpServletRequest httpServletRequest = Mockito.mock(HttpServletRequest.class);
    private AccessEvent accessEvent;

    @Before
    public void setUp() throws Exception {
        accessEvent = new AccessEvent(httpServletRequest, Mockito.mock(HttpServletResponse.class),
            Mockito.mock(ServerAdapter.class));

        safeRequestParameterConverter.setOptionList(ImmutableList.of("name"));
        safeRequestParameterConverter.start();
    }

    @After
    public void tearDown() throws Exception {
        safeRequestParameterConverter.stop();
    }

    @Test
    public void testConvertOneParameter() throws Exception {
        Mockito.when(httpServletRequest.getParameterValues("name")).thenReturn(new String[]{"Alice"});
        final Vector<String> parameterNames = new Vector<>();
        parameterNames.add("name");
        Mockito.when(httpServletRequest.getParameterNames()).thenReturn(parameterNames.elements());

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
        final Vector<String> parameterNames = new Vector<>();
        parameterNames.add("name");
        Mockito.when(httpServletRequest.getParameterNames()).thenReturn(parameterNames.elements());

        // Invoked by AccessEvent#prepareForDeferredProcessing
        accessEvent.buildRequestParameterMap();
        // Jetty recycled the request
        Mockito.reset(httpServletRequest);

        final String value = safeRequestParameterConverter.convert(accessEvent);
        assertThat(value).isEqualTo("[Alice, Bob]");
    }

    @Test
    public void testGetUnknownParameter() {
        final String value = safeRequestParameterConverter.convert(accessEvent);
        assertThat(value).isEqualTo("-");
    }

}
