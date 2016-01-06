package io.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.validation.constraints.NotNull;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewBundleTest {
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private static class MyConfiguration extends Configuration {
        @NotNull
        private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

        @JsonProperty("viewRendererConfiguration")
        public Map<String, Map<String, String>> getViewRendererConfiguration() {
            return viewRendererConfiguration;
        }

        @JsonProperty("viewRendererConfiguration")
        public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
            ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();
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
        new ViewBundle<>().run(null, environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void addsTheViewMessageBodyWriterWithSingleViewRendererToTheEnvironment() throws Exception {
        final String viewSuffix = ".ftl";
        final String testKey = "testKey";
        final Map<String, Map<String, String>> viewRendererConfig = new HashMap<>();
        final Map<String, String> freeMarkerConfig = new HashMap<>();
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
            public void configure(Map<String, String> options) {
                assertThat("should contain the testKey", Boolean.TRUE, is(options.containsKey(testKey)));
            }

            @Override
            public String getSuffix() {
                return viewSuffix;
            }
        };

        new ViewBundle<MyConfiguration>(ImmutableList.of(renderer)) {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(MyConfiguration configuration) {
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
