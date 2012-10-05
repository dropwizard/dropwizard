package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.config.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * A basic CLI command.
 */
public abstract class Command {
    private final String name;
    private final String description;

    protected Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Executes when the user runs this specific command.
     *
     * @param bootstrap    the bootstrap bootstrap
     * @throws Exception if something goes wrong
     */
    public abstract void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception;

    public abstract void configure(Subparser subparser);
}
