package com.codahale.dropwizard.server;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;

public abstract class ServerApplication<T extends ServerConfiguration> extends Application<T>
{
    @Override
    public final void run(T configuration, Environment environment)
        throws Exception
    {
        run(configuration, (ServerEnvironment) environment);
    }
    
    @Override
    public final void initialize(Bootstrap<T> bootstrap)    {
        bootstrap.addCommand(new ServerCommand(this));
        initializeServer(bootstrap);
    }
    
    protected abstract void initializeServer(Bootstrap<T> bootstrap);

    public abstract void run(T configuration, ServerEnvironment environment)
        throws Exception;

}
