package com.yammer.dropwizard.jetty;

import com.yammer.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.component.AbstractLifeCycle;

// TODO: 10/12/11 <coda> -- write tests for JettyManaged
// TODO: 10/12/11 <coda> -- write docs for JettyManaged

public class JettyManaged extends AbstractLifeCycle implements Managed {
    private final Managed managed;

    public JettyManaged(Managed managed) {
        this.managed = managed;
    }

    @Override
    protected void doStart() throws Exception {
        managed.start();
    }

    @Override
    protected void doStop() throws Exception {
        managed.stop();
    }
}
