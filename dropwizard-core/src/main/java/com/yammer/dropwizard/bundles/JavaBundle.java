package com.yammer.dropwizard.bundles;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;

/**
 * Initializes the service with support for Java classes.
 */
public class JavaBundle implements Bundle {
    public static final ImmutableList<Class<?>> DEFAULT_PROVIDERS = ImmutableList.<Class<?>>of(
            OptionalQueryParamInjectableProvider.class
    );

    private final Service<?> service;

    public JavaBundle(Service<?> service) {
        this.service = service;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new JacksonMessageBodyProvider(service.getJson()));
        for (Class<?> provider : DEFAULT_PROVIDERS) {
            environment.addProvider(provider);
        }
    }
}
