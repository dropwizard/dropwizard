package com.yammer.dropwizard.config.tests;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.junit.Test;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.mockito.Mockito.mock;

public class EnvironmentTest {
    static {
        Logger.getLogger("com.sun.jersey").setLevel(Level.OFF);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void scanPackagesHandlesEmptyArgumentList() {
        new Environment("", mock(Configuration.class), mock(ObjectMapperFactory.class)).scanPackagesForResourcesAndProviders();
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void scanPackagesThrowsNpeOnNullArgument() {
        new Environment("", mock(Configuration.class), mock(ObjectMapperFactory.class)).scanPackagesForResourcesAndProviders(
                (Class<?>[]) null);
    }
}
