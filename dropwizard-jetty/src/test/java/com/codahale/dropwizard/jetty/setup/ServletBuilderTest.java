package com.codahale.dropwizard.jetty.setup;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class ServletBuilderTest {
    private final ServletHolder holder = mock(ServletHolder.class);
    private final ServletContextHandler handler = mock(ServletContextHandler.class);
    private final ServletBuilder config = new ServletBuilder(holder, handler);

    @Test
    public void setsInitializationOrder() throws Exception {
        config.setInitOrder(13);

        verify(holder).setInitOrder(13);
    }

    @Test
    public void setsInitializationParameters() throws Exception {
        config.setInitParam("one", "1");

        verify(holder).setInitParameter("one", "1");
    }

    @Test
    public void addsInitializationParameters() throws Exception {
        config.addInitParams(ImmutableMap.of("one", "1", "two", "2"));

        verify(holder).setInitParameter("one", "1");
        verify(holder).setInitParameter("two", "2");
        verifyNoMoreInteractions(holder);
    }

    @Test
    public void mapsAUrlPatternToAServlet() throws Exception {
        config.addUrlPattern("/one");

        verify(handler).addServlet(holder, "/one");
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void mapsUrlPatternsToAServlet() throws Exception {
        config.addUrlPatterns("/one", "/two");

        verify(handler).addServlet(holder, "/one");
        verify(handler).addServlet(holder, "/two");
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void setsTheNameForAServlet() throws Exception {
        config.setName("poop");

        verify(holder).setName("poop");
    }
}
