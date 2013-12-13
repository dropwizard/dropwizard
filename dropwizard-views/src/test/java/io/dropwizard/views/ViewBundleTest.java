package io.dropwizard.views;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.WebApplicationException;

import com.google.common.collect.ImmutableList;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ViewBundleTest {
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);

    @Before
    public void setUp() throws Exception {
        when(environment.jersey()).thenReturn(jerseyEnvironment);
    }

    @Test
    public void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle().run(environment);

        verify(jerseyEnvironment).register(any(ViewMessageBodyWriter.class));
    }

    @Test
    public void addsTheViewMessageBodyWriterWithSingleViewRendererToTheEnvironment() throws Exception {
        ViewRenderer renderer = new ViewRenderer() {
            @Override
            public boolean isRenderable(View view) {
                return false;
            }

            @Override
            public void render(View view, Locale locale, OutputStream output) throws IOException, WebApplicationException {
                //nothing to do
            }
        };

        new ViewBundle(ImmutableList.of(renderer)).run(environment);

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
