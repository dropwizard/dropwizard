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

    /**
     * Create a new command with the given name and description.
     *
     * @param name        the name of the command, used for command line invocation
     * @param description a description of the command's purpose
     */
    protected Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * Returns the command's name.
     *
     * @return the command's name
     */
    public final String getName() {
        return name;
    }

    /**
     * Returns the command's description.
     *
     * @return the command's description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Configure the command's {@link Subparser}.
     *
     * @param subparser the {@link Subparser} specific to the command
     */
    public abstract void configure(Subparser subparser);

    /**
     * Executes when the user runs this specific command.
     *
     * @param bootstrap the bootstrap bootstrap
     * @param namespace the parsed command line namespace
     * @throws Exception if something goes wrong
     */
    public abstract void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception;
}
