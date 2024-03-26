package com.example.sslreload;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.core.sslreload.SslReloadBundle;
import io.dropwizard.core.sslreload.SslReloadTask;

public class SslReloadApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new SslReloadTask());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        // Nothing to do
    }
}
