package com.codahale.dropwizard.jetty.setup;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.junit.Test;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static org.mockito.Mockito.*;

public class FilterBuilderTest {
    private final FilterHolder holder = mock(FilterHolder.class);
    private final ServletContextHandler handler = mock(ServletContextHandler.class);
    private final FilterBuilder config = new FilterBuilder(holder, handler);

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
    public void mapsAUrlPatternToAFilter() throws Exception {
        config.addUrlPattern("/one");

        verify(handler).addFilter(holder, "/one", EnumSet.of(DispatcherType.REQUEST));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void mapsUrlPatternsToAFilter() throws Exception {
        config.addUrlPatterns("/one", "/two");

        verify(handler).addFilter(holder, "/one", EnumSet.of(DispatcherType.REQUEST));
        verify(handler).addFilter(holder, "/two", EnumSet.of(DispatcherType.REQUEST));
        verifyNoMoreInteractions(handler);
    }

    @Test
    public void setsTheNameForAFilter() throws Exception {
        config.setName("poop");

        verify(holder).setName("poop");
    }
}
