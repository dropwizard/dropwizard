package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.config.Bootstrap;

/**
 * A basic CLI command.
 */
public interface Command {
    /**
     * Executes when the user runs this specific command.
     *
     * @param bootstrap    the bootstrap bootstrap
     * @throws Exception if something goes wrong
     */
    void run(Bootstrap<?> bootstrap) throws Exception;
}
