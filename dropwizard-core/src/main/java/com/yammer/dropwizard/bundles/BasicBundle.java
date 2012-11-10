package com.yammer.dropwizard.bundles;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;

/**
 * Initializes the service with support for basic Java classes.
 */
public class BasicBundle implements Bundle {
    public static final ImmutableList<Class<?>> DEFAULT_PROVIDERS = ImmutableList.<Class<?>>of(
            OptionalQueryParamInjectableProvider.class
    );

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(Environment environment) {
        environment.addProvider(new JacksonMessageBodyProvider(environment.getObjectMapperFactory().build()));
        for (Class<?> provider : DEFAULT_PROVIDERS) {
            environment.addProvider(provider);
        }
    }
}
