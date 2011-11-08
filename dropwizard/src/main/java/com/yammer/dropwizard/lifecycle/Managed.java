package com.yammer.dropwizard.lifecycle;

// TODO: 10/12/11 <coda> -- write docs for Managed

public interface Managed {
    public void start() throws Exception;

    public void stop() throws Exception;
}
