package io.dropwizard;

import io.dropwizard.cli.ServerCommand;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.setup.HttpBootstrap;
import io.dropwizard.setup.HttpEnvironment;

public abstract class HttpApplication<T extends HttpConfiguration> extends Application<T> {

    @Override
    protected Bootstrap<T> createBootstrap() {
        final Bootstrap<T> bootstrap = new HttpBootstrap<>(this);
        bootstrap.addCommand(new ServerCommand<>(this));
        return bootstrap;
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        if (environment instanceof HttpEnvironment) {
            run(configuration, (HttpEnvironment) environment);
        } else {
            throw new IllegalStateException("HttpApplication should only receive HttpEnvironment");
        }
    }

    public abstract void run(T configuration, HttpEnvironment environment) throws Exception;

}
