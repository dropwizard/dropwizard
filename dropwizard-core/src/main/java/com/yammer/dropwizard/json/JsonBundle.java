package com.yammer.dropwizard.json;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;

/**
 * Initializes the service with support for basic Java classes.
 */
public class JsonBundle implements Bundle {
    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        // nothing doing
    }

    @Override
    public void run(Environment environment) {
        environment.addProvider(new JacksonMessageBodyProvider(environment.getObjectMapperFactory().build()));
    }
}
