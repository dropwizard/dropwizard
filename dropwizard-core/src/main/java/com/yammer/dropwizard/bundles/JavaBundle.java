package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OauthTokenProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Initializes the service with support for Java classes.
 */
public class JavaBundle implements Bundle {
    public static final List<Object> DEFAULT_PROVIDERS = asList(
            new OptionalQueryParamInjectableProvider(),
            new JacksonMessageBodyProvider(),
            new OauthTokenProvider()
    );

    @Override
    public void initialize(Environment environment) {
        for (Object provider : DEFAULT_PROVIDERS) {
            environment.addProvider(provider);
        }
    }
}
