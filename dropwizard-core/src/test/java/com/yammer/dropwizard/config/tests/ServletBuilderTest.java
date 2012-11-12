package com.yammer.dropwizard.config.tests;

import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.config.ServletBuilder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServletBuilderTest {
    private final ServletHolder holder = mock(ServletHolder.class);
    private final ImmutableMap.Builder<String, ServletHolder> mappings = ImmutableMap.builder();
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

        assertThat(mappings.build())
                .isEqualTo(ImmutableMap.of("/one", holder));
    }

    @Test
    public void mapsUrlPatternsToAServlet() throws Exception {
        config.addUrlPatterns("/one", "/two");

        assertThat(mappings.build())
                .isEqualTo(ImmutableMap.of("/one", holder,
                                           "/two", holder));
    }

    @Test
    public void setsTheNameForAServlet() throws Exception {
        config.setName("poop");

        verify(holder).setName("poop");
    }
}
