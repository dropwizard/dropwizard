package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;

/**
 * A basic CLI command.
 */
public interface Command {
    /**
     * Executes when the user runs this specific command.
     *
     * @param service the service to which the command belongs
     * @throws Exception if something goes wrong
     */
    void run(AbstractService<?> service) throws Exception;
}
