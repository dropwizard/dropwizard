package com.codahale.dropwizard.server;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.ServerConfiguration;
import com.codahale.dropwizard.cli.ServiceCommand;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.util.concurrent.Service;

public class ServerCommand<T extends ServerConfiguration> extends ServiceCommand<T>
{
    public ServerCommand(Application<T> application)
    {
        super(application, "server", "Runs the Dropwizard application as an HTTP server");
    }

    @Override
    protected Service buildService(Environment environment, T configuration)
    {
        return configuration.getServerFactory().build(environment);
    }
}