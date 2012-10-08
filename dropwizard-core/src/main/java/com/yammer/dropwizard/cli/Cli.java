package com.yammer.dropwizard.cli;

import com.google.common.collect.Maps;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.action.VersionArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;

/**
 * The command-line runner for Dropwizard services.
 */
public class Cli {
    private static final String COMMAND_NAME_ATTR = "command";
    private static final String[] HELP = { "-h" };

    private final SortedMap<String, Command> commands;
    private final Bootstrap<?> bootstrap;
    private final ArgumentParser parser;

    /**
     * Create a new CLI interface for a service and its bootstrapped environment.
     *
     * @param serviceClass the service class
     * @param bootstrap    the bootstrap for the service
     */
    public Cli(Class<?> serviceClass, Bootstrap<?> bootstrap) {
        this.commands = Maps.newTreeMap();
        this.parser = buildParser(serviceClass);
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

    private ArgumentParser buildParser(Class<?> serviceClass) {
        final String usage = "java -jar " + new JarLocation(serviceClass);
        final ArgumentParser p = ArgumentParsers.newArgumentParser(usage)
                                                .defaultHelp(true);
        String version = serviceClass.getPackage().getImplementationVersion();
        if (version == null) {
            version = "No service version detected. Add a Implementation-Version " +
                    "entry to your JAR's manifest to enable this.";
        }
        p.version(version);
        p.addArgument("-v", "--version")
         .action(new VersionArgumentAction())
         .help("show the service version and exit");
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
