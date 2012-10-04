package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of service behavior.
 */
@SuppressWarnings("UnusedParameters")
public abstract class Bundle {
    public void initialize(Bootstrap<?> bootstrap) {

    }

    public void run(Environment environment) {

    }
}
