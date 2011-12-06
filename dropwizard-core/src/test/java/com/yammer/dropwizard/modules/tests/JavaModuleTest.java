package com.yammer.dropwizard.modules.tests;

import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OauthTokenProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;
import com.yammer.dropwizard.modules.JavaModule;
import org.junit.Test;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JavaModuleTest {
    private final Environment environment = mock(Environment.class);
    private final JavaModule module = new JavaModule();

    @Test
    public void addsOAuthSupport() throws Exception {
        module.initialize(environment);

        verify(environment).addProvider(isA(OauthTokenProvider.class));
    }

    @Test
    public void addsJSONSupport() throws Exception {
        module.initialize(environment);
        
        verify(environment).addProvider(isA(JacksonMessageBodyProvider.class));
    }

    @Test
    public void addsOptionalQueryParamSupport() throws Exception {
        module.initialize(environment);

        verify(environment).addProvider(isA(OptionalQueryParamInjectableProvider.class));
    }
}
