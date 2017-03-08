package com.example.badlog;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BadLogApp extends Application<Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BadLogApp.class);

    @Override
    protected void onFatalError() {
        LOGGER.warn("Mayday we're going down");
    }

    public static void runMe(String[] args) throws Exception {
        new BadLogApp().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        throw new RuntimeException("I'm a bad app");
    }
}
