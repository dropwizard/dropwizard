package io.dropwizard.views.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.OutputStream;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViewBundleTest {
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

    @BeforeEach
    void setUp() {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
    }

    @Test
    void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle<>().run(new MyConfiguration(), environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));
    }

    @Test
    void addsTheViewMessageBodyWriterWithSingleViewRendererToTheEnvironment() throws Exception {
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
            public void render(View view, Locale locale, OutputStream output) throws WebApplicationException {
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
        assertThat(capturedRenderer.getRenderers())
            .hasSize(1)
            .contains(renderer);
    }
}
