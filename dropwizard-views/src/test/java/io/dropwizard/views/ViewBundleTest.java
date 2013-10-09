package io.dropwizard.views;

import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;

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
}
