package com.yammer.dropwizard.bundles.tests;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.bundles.JavaBundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;
import com.yammer.dropwizard.json.Json;
import org.codehaus.jackson.map.Module;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.*;

public class JavaBundleTest {
    private final Environment environment = mock(Environment.class);
    private final Service<?> service = mock(Service.class);
    private final Json json = mock(Json.class);
    private final JavaBundle bundle = new JavaBundle(service);

    @Before
    public void setUp() throws Exception {
        when(service.getJson()).thenReturn(json);
        when(service.getJacksonModules()).thenReturn(ImmutableList.<Module>of());
    }

    @Test
    public void addsJSONSupport() throws Exception {
        bundle.initialize(environment);
        
        verify(environment).addProvider(isA(JacksonMessageBodyProvider.class));
    }

    @Test
    public void addsOptionalQueryParamSupport() throws Exception {
        bundle.initialize(environment);

        verify(environment).addProvider(eq(OptionalQueryParamInjectableProvider.class));
    }
}
