package com.yammer.dropwizard.bundles;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.jersey.OauthTokenProvider;
import com.yammer.dropwizard.jersey.OptionalQueryParamInjectableProvider;

/**
 * Initializes the service with support for Java classes.
 */
public class JavaBundle implements Bundle {
    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new OptionalQueryParamInjectableProvider());
        environment.addProvider(new JacksonMessageBodyProvider());
        environment.addProvider(new OauthTokenProvider());
    }
}
