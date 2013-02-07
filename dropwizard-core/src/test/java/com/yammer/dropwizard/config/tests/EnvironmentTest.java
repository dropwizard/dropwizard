package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.mockito.Mockito.mock;

public class EnvironmentTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void scanPackagesHandlesEmptyArgumentList() {
        new Environment("",
                        mock(Configuration.class),
                        mock(ObjectMapperFactory.class),
                        new Validator()).scanPackagesForResourcesAndProviders();
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void scanPackagesThrowsNpeOnNullArgument() {
        new Environment("",
                        mock(Configuration.class),
                        mock(ObjectMapperFactory.class),
                        new Validator()).scanPackagesForResourcesAndProviders((Class<?>[]) null);
    }
}
