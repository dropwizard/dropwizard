package io.dropwizard.documentation;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.core.sslreload.SslReloadBundle;

public class SslReloadApp extends Application<Configuration> {
    @Override
    // core: SslReloadApp#initialize
    public void initialize(Bootstrap<Configuration> bootstrap) {
        bootstrap.addBundle(new SslReloadBundle());
    }
    // core: SslReloadApp#initialize

    @Override
    public void run(Configuration configuration, Environment environment) {}
}
