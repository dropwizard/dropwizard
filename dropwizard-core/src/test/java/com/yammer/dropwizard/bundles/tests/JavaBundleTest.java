package com.yammer.dropwizard.bundles.tests;

import com.yammer.dropwizard.bundles.BasicBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class JavaBundleTest {
    private final ObjectMapperFactory factory = mock(ObjectMapperFactory.class);
    private final Environment environment = mock(Environment.class);
    private final BasicBundle bundle = new BasicBundle();

    @Before
    public void setUp() throws Exception {
        when(environment.getObjectMapperFactory()).thenReturn(factory);
    }

    @Test
    public void addsJSONSupport() throws Exception {
        bundle.run(environment);
        
        verify(environment).addProvider(isA(JacksonMessageBodyProvider.class));
    }

    @Test
    public void addsOptionalQueryParamSupport() throws Exception {
        bundle.run(environment);

        verify(environment).addProvider(eq(OptionalQueryParamInjectableProvider.class));
    }
}
