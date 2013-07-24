package com.codahale.dropwizard.lifecycle;

import com.google.common.base.Throwables;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

public class ServiceManaged implements Service.Listener
{
    private final Managed managed;

    public ServiceManaged(Managed managed)
    {
        this.managed = managed;
    }

    public void starting()
    {
        try {
            managed.start();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void running()
    {
    }

    public void stopping(State from)
    {
        try {
            managed.stop();
        }
        catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public void terminated(State from)
    {
    }

    public void failed(State from, Throwable failure)
    {
    }
}
