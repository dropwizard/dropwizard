package com.yammer.dropwizard.views.tests;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.setup.JerseyEnvironment;
import com.yammer.dropwizard.views.ViewBundle;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ViewBundleTest {
    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
    private final Environment environment = mock(Environment.class);

    @Before
    public void setUp() throws Exception {
        when(environment.getJerseyEnvironment()).thenReturn(jerseyEnvironment);
    }

    @Test
    public void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle().run(environment);

        verify(jerseyEnvironment).addProvider(ViewMessageBodyWriter.class);
    }
}
