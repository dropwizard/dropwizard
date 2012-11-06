package com.yammer.dropwizard.config.tests;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.yammer.dropwizard.config.FilterBuilder;
import org.eclipse.jetty.servlet.FilterHolder;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class FilterBuilderTest {
    private final FilterHolder holder = mock(FilterHolder.class);
    private final ImmutableMultimap.Builder<String, FilterHolder> mappings = ImmutableMultimap.builder();
    private final FilterBuilder config = new FilterBuilder(holder, mappings);

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

        assertThat(mappings.build())
                .isEqualTo(ImmutableMultimap.of("/one", holder));
    }

    @Test
    public void mapsUrlPatternsToAFilter() throws Exception {
        config.addUrlPatterns("/one", "/two");

        assertThat(mappings.build())
                .isEqualTo(ImmutableMultimap.of("/one", holder,
                                                "/two", holder));
    }

    @Test
    public void setsTheNameForAFilter() throws Exception {
        config.setName("poop");

        verify(holder).setName("poop");
    }
}
