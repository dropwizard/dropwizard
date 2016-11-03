package com.example.sslreload;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.sslreload.SslReloadBundle;

public class SslReloadApp extends Application<Configuration> {
    @Override
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new SslReloadBundle());
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
    }
}
