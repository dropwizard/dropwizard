package com.codahale.dropwizard.server;

import com.codahale.dropwizard.Bundle;
import com.codahale.dropwizard.setup.Environment;

public abstract class ServerBundle implements Bundle
{
    public final void run(Environment environment)
    {
        if (!(environment instanceof ServerEnvironment)) {
            throw new AssertionError();
        }
        run((ServerEnvironment) environment);
    }

    public abstract void run(ServerEnvironment environment);

}
