package com.yammer.dropwizard.setup.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yammer.dropwizard.setup.ServletBuilder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServletBuilderTest {
    private final ServletHolder holder = mock(ServletHolder.class);
    private final Map<String, ServletHolder> mappings = Maps.newLinkedHashMap();
    private final ServletBuilder config = new ServletBuilder(holder, mappings);

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

        assertThat(mappings)
                .isEqualTo(ImmutableMap.of("/one", holder));
    }

    @Test
    public void mapsUrlPatternsToAServlet() throws Exception {
        config.addUrlPatterns("/one", "/two");

        assertThat(mappings)
                .isEqualTo(ImmutableMap.of("/one", holder,
                                           "/two", holder));
    }

    @Test
    public void setsTheNameForAServlet() throws Exception {
        config.setName("poop");

        verify(holder).setName("poop");
    }
}
