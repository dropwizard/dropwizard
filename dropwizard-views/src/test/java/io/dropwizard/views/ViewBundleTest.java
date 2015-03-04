package io.dropwizard.views;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ViewBundleTest {
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private static class MyConfiguration extends Configuration {
        @NotNull
        private ImmutableMap<String, ImmutableMap<String, String>> viewRendererConfiguration = ImmutableMap.of();

        @JsonProperty("viewRendererConfiguration")
        public ImmutableMap<String, ImmutableMap<String, String>> getViewRendererConfiguration() {
            return viewRendererConfiguration;
        }

        @JsonProperty("viewRendererConfiguration")
        public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
            ImmutableMap.Builder<String, ImmutableMap<String, String>> builder = ImmutableMap.builder();
            for (Map.Entry<String, Map<String, String>> entry : viewRendererConfiguration.entrySet()) {
                builder.put(entry.getKey(), ImmutableMap.copyOf(entry.getValue()));
            }
            this.viewRendererConfiguration = builder.build();
        }
    }

    @Before
    public void setUp() throws Exception {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
    }

    @Test
    public void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle() {
            @Override
            public ImmutableMap<String, Map<String, String>> getViewConfiguration(Configuration configuration) {
                return ImmutableMap.of();
            }
        }.run(null, environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));
    }

    @Test
    public void addsTheViewMessageBodyWriterWithSingleViewRendererToTheEnvironment() throws Exception {
        final String viewSuffix = ".ftl";
        final String testKey = "testKey";
        final Map<String, Map<String, String>> viewRendererConfig = Maps.newHashMap();
        final Map<String, String> freeMarkerConfig = Maps.newHashMap();
        freeMarkerConfig.put(testKey, "yes");
        viewRendererConfig.put(viewSuffix, freeMarkerConfig);
        
        MyConfiguration myConfiguration = new MyConfiguration();
        myConfiguration.setViewRendererConfiguration(viewRendererConfig);
        
        ViewRenderer renderer = new ViewRenderer() {
            @Override
            public boolean isRenderable(View view) {
                return false;
            }

            @Override
            public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException {
                //nothing to do
            }

            @Override
            public void configure(ImmutableMap<String, String> options) {
                assertThat("should contain the testKey", Boolean.TRUE, is(options.containsKey(testKey)));
            }

            @Override
            public String getSuffix() {
                return viewSuffix;
            }
        };

        new ViewBundle<MyConfiguration>(ImmutableList.of(renderer)) {
            @Override
            public ImmutableMap<String, ImmutableMap<String, String>> getViewConfiguration(MyConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        }.run(myConfiguration, environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));

        ArgumentCaptor<ViewMessageBodyWriter> argumentCaptor = ArgumentCaptor.forClass(ViewMessageBodyWriter.class);
        verify(jerseyEnvironment).register(argumentCaptor.capture());

        Field renderers = ViewMessageBodyWriter.class.getDeclaredField("renderers");
        renderers.setAccessible(true);
        List<ViewRenderer> configuredRenderers = ImmutableList.copyOf((Iterable<ViewRenderer>) renderers.get(argumentCaptor.getValue()));
        assertEquals(1, configuredRenderers.size());
        assertTrue(configuredRenderers.contains(renderer));
    }
}
