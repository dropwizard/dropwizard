package com.yammer.dropwizard.views.tests;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import com.yammer.dropwizard.views.ViewMessageBodyWriter;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ViewBundleTest {
    private final Environment environment = mock(Environment.class);

    @Test
    public void addsTheViewMessageBodyWriterToTheEnvironment() throws Exception {
        new ViewBundle().run(environment);

        verify(environment).addProvider(ViewMessageBodyWriter.class);
    }
}
