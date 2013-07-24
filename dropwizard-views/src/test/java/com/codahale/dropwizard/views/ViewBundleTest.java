package com.codahale.dropwizard.views;

import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.server.ServerEnvironment;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ViewBundleTest {
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final ServerEnvironment environment = mock(ServerEnvironment.class);

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
