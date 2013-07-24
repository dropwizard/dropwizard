package com.codahale.dropwizard.server;

import com.codahale.dropwizard.ConfiguredBundle;
import com.codahale.dropwizard.setup.Environment;

public abstract class ConfiguredServerBundle<T> implements ConfiguredBundle<T>
{
    public final void run(T configuration, Environment environment) throws Exception
    {
        if (!(environment instanceof ServerEnvironment)) {
            throw new AssertionError();
        }
        run(configuration, (ServerEnvironment) environment);
    }

    public abstract void run(T configuration, ServerEnvironment environment)
        throws Exception;

}
