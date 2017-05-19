package io.dropwizard.cdi;

import io.dropwizard.lifecycle.Managed;
import org.jboss.weld.environment.se.Weld;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ManagedWeld implements Managed {
    private static final Logger log = LoggerFactory.getLogger(ManagedWeld.class);
    private final Weld weld;

    ManagedWeld() {
        weld = new Weld();
    }

    @Override
    public void start() throws Exception {
        weld.initialize();
        log.info("Started WeldContainer");
    }

    @Override
    public void stop() throws Exception {
        weld.shutdown();
        log.info("Stopped WeldContainer");
    }
}
