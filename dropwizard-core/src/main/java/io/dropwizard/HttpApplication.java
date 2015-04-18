package io.dropwizard;

import io.dropwizard.cli.ServerCommand;
import io.dropwizard.setup.Bootstrap;

public abstract class HttpApplication<T extends HttpConfiguration> extends Application<T> {

    @Override
    protected Bootstrap<T> createBootstrap() {
        final Bootstrap<T> bootstrap = new Bootstrap<>(this);
        bootstrap.addCommand(new ServerCommand<>(this));
        return bootstrap;
    }

}
