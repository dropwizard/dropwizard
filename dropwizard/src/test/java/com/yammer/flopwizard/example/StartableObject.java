package com.yammer.flopwizard.example;

import com.yammer.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartableObject implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartableObject.class);

    @Override
    public void start() throws Exception {
        try {
            throw new RuntimeException("fuuuck");
        } catch (RuntimeException e) {
            LOGGER.error("Dang", e);
        }
        LOGGER.info("Starting");
    }

    /*
     * N.B.: This actually gets called, but if you're running it through Maven
     * and you hit ^C you won't see the final set of log statements indicating
     * that it actually does the full shutdown. It does. kill -SIGINT the JVM
     * instead of hitting ^C and you'll see the log statements.
     */
    @Override
    public void stop() throws Exception {
        LOGGER.info("Stopping");
    }
}
