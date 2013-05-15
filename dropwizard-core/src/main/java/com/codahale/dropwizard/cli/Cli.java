package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.util.JarLocation;
import com.google.common.collect.Maps;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.action.VersionArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;

// TODO: 5/15/13 <coda> -- add tests for Cli

/**
 * The command-line runner for Dropwizard application.
 */
public class Cli {
    private static final String COMMAND_NAME_ATTR = "command";
    private static final String[] HELP = { "-h" };

    private final SortedMap<String, Command> commands;
    private final Bootstrap<?> bootstrap;
    private final ArgumentParser parser;

    /**
     * Create a new CLI interface for a application and its bootstrapped environment.
     *
     * @param location     the location of the application
     * @param bootstrap    the bootstrap for the application
     */
    public Cli(JarLocation location, Bootstrap<?> bootstrap) {
        this.commands = Maps.newTreeMap();
        this.parser = buildParser(location);
        this.bootstrap = bootstrap;
        for (Command command : bootstrap.getCommands()) {
            addCommand(command);
        }
    }

    /**
     * Runs the command line interface given some arguments.
     *
     * @param arguments the command line arguments
     * @throws Exception if something goes wrong
     */
    public void run(String[] arguments) throws Exception {
        try {
            // assume -h if no arguments are given
            final String[] args = (arguments.length == 0) ? HELP : arguments;
            final Namespace namespace = parser.parseArgs(args);
            final Command command = commands.get(namespace.getString(COMMAND_NAME_ATTR));
            command.run(bootstrap, namespace);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }

    private ArgumentParser buildParser(JarLocation location) {
        final String usage = "java -jar " + location;
        final ArgumentParser p = ArgumentParsers.newArgumentParser(usage)
                                                .defaultHelp(true);
        p.version(location.getVersion().or(
                "No application version detected. Add a Implementation-Version " +
                        "entry to your JAR's manifest to enable this."));
        p.addArgument("-v", "--version")
         .action(new VersionArgumentAction())
         .help("show the application version and exit");
        return p;
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
        parser.addSubparsers().help("available commands");
        final Subparser subparser = parser.addSubparsers().addParser(command.getName());
        command.configure(subparser);
        subparser.description(command.getDescription())
                 .setDefault(COMMAND_NAME_ATTR, command.getName())
                 .defaultHelp(true);
    }
}
