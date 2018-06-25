package io.dropwizard.views;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewBundleTest {
    private JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private Environment environment = mock(Environment.class);

    private static class MyConfiguration extends Configuration {
        @NotNull
        private Map<String, Map<String, String>> viewRendererConfiguration = Collections.emptyMap();

        @JsonProperty("viewRendererConfiguration")
        public Map<String, Map<String, String>> getViewRendererConfiguration() {
            return viewRendererConfiguration;
        }

        @JsonProperty("viewRendererConfiguration")
        public void setViewRendererConfiguration(Map<String, Map<String, String>> viewRendererConfiguration) {
            this.viewRendererConfiguration = viewRendererConfiguration;
        }
    }

    @Before
    public void setUp() throws Exception {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
    }

    @Test
    public void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle<>().run(new MyConfiguration(), environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));
    }

    @Test
    public void addsTheViewMessageBodyWriterWithSingleViewRendererToTheEnvironment() throws Exception {
        final String configurationKey = "freemarker";
        final String testKey = "testKey";
        final Map<String, String> freeMarkerConfig = Collections.singletonMap(testKey, "yes");
        final Map<String, Map<String, String>> viewRendererConfig = Collections.singletonMap(configurationKey, freeMarkerConfig);

        final MyConfiguration myConfiguration = new MyConfiguration();
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
                assertThat(options).containsKey(testKey);
            }

            @Override
            public String getConfigurationKey() {
                return configurationKey;
            }
        };

        new ViewBundle<MyConfiguration>(Collections.singletonList(renderer)) {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(MyConfiguration configuration) {
                return configuration.getViewRendererConfiguration();
            }
        }.run(myConfiguration, environment);

        final ArgumentCaptor<ViewMessageBodyWriter> captor = ArgumentCaptor.forClass(ViewMessageBodyWriter.class);
        verify(jerseyEnvironment).register(captor.capture());

        final ViewMessageBodyWriter capturedRenderer = captor.getValue();
        final Iterable<ViewRenderer> configuredRenderers = capturedRenderer.getRenderers();
        assertThat(configuredRenderers).hasSize(1);
        assertThat(configuredRenderers).contains(renderer);
    }
}
