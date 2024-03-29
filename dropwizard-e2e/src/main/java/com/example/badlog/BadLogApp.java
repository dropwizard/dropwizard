package com.example.badlog;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BadLogApp extends Application<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadLogApp.class);

    @Override
    protected void onFatalError(Throwable t) {
        LOGGER.warn("Mayday we're going down");
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        throw new RuntimeException("I'm a bad app");
    }
}
