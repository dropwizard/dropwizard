package com.yammer.dropwizard.config.tests;

import static org.mockito.Mockito.mock;

import org.junit.Test;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;

public class EnvironmentTest {

    @Test
    @SuppressWarnings("unchecked")
    public void scanPackagesHandlesEmptyArgumentList() {
        new Environment(mock(AbstractService.class), mock(Configuration.class)).scanPackagesForResourcesAndProviders();
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("unchecked")
    public void scanPackagesThrowsNpeOnNullArgument() {
        new Environment(mock(AbstractService.class), mock(Configuration.class)).scanPackagesForResourcesAndProviders(
                (Class<?>[]) null);
    }
}
