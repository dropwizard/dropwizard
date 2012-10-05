package com.yammer.dropwizard.cli;

import com.google.common.collect.Maps;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.util.SortedMap;

public class Cli {
    private static final String COMMAND_NAME_ATTR = "command";

    private final SortedMap<String, Command> commands;
    private final Bootstrap<?> bootstrap;
    private final ArgumentParser parser;

    public Cli(Bootstrap<?> bootstrap, Service<?> service) {
        this.commands = Maps.newTreeMap();
        this.parser = ArgumentParsers.newArgumentParser("java -jar " + new JarLocation(service.getClass()))
                                     .defaultHelp(true);
        parser.defaultHelp(true);
        this.bootstrap = bootstrap;
        for (Command command : bootstrap.getCommands()) {
            addCommand(command);
        }
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
        parser.addSubparsers().help("Available commands");
        final Subparser subparser = parser.addSubparsers().addParser(command.getName());
        command.configure(subparser);
        subparser.description(command.getDescription())
                 .setDefault(COMMAND_NAME_ATTR, command.getName())
                 .defaultHelp(true);
    }

    public void run(String[] arguments) throws Exception {
        try {
            final Namespace namespace = parser.parseArgs((arguments.length == 0) ? new String[]{ "-h" } : arguments);
            final Command command = commands.get(namespace.getString(COMMAND_NAME_ATTR));
            command.run(bootstrap, namespace);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
    }
}
