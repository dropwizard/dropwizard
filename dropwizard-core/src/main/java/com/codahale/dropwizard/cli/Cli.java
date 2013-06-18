package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.util.JarLocation;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.*;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.SortedMap;

/**
 * The command-line runner for Dropwizard application.
 */
public class Cli {
    private static final String COMMAND_NAME_ATTR = "command";
    // assume -h if no arguments are given
    private static final String[][] HELP = {{}, {"-h"}, {"--help"}};
    private static final String[][] VERSION = {{"-v"}, {"--version"}};

    private final PrintWriter stdOut;
    private final PrintWriter stdErr;
    private final SortedMap<String, Command> commands;
    private final Bootstrap<?> bootstrap;
    private final ArgumentParser parser;

    /**
     * Create a new CLI interface for a application and its bootstrapped environment.
     *
     * @param location     the location of the application
     * @param bootstrap    the bootstrap for the application
     * @param stdOut       standard out
     * @param stdErr       standard err
     */
    public Cli(JarLocation location, Bootstrap<?> bootstrap, OutputStream stdOut, OutputStream stdErr) {
        this.stdOut = new PrintWriter(new OutputStreamWriter(stdOut, Charsets.UTF_8), true);
        this.stdErr = new PrintWriter(new OutputStreamWriter(stdErr, Charsets.UTF_8), true);
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
     * @return whether or not the command successfully executed
     * @throws Exception if something goes wrong
     */
    public boolean run(String... arguments) throws Exception {
        try {
            if (isFlag(HELP, arguments)) {
                parser.printHelp(stdOut);
            } else if (isFlag(VERSION, arguments)) {
                parser.printVersion(stdOut);
            } else {
                final Namespace namespace = parser.parseArgs(arguments);
                if (namespace.get("is-help") == null) {
                    final Command command = commands.get(namespace.getString(COMMAND_NAME_ATTR));
                    command.run(bootstrap, namespace);
                }
            }
            return true;
        } catch (ArgumentParserException e) {
            // TODO: 5/25/13 <coda> -- make ArgumentParser#handleError not depend on System.err
            stdErr.println(e.getMessage());
            e.getParser().printHelp(stdErr);
            return false;
        }
    }

    private boolean isFlag(String[][] flags, String[] arguments) {
        for (String[] cmd : flags) {
            if (Arrays.equals(arguments, cmd)) {
                return true;
            }
        }
        return false;
    }

    private ArgumentParser buildParser(JarLocation location) {
        final String usage = "java -jar " + location;
        final ArgumentParser p = ArgumentParsers.newArgumentParser(usage, false);
        p.version(location.getVersion().or(
                "No application version detected. Add a Implementation-Version " +
                        "entry to your JAR's manifest to enable this."));
        addHelp(p);
        p.addArgument("-v", "--version")
         .action(Arguments.help()) // never gets called; intercepted in #run
         .help("show the application version and exit");
        return p;
    }

    private void addHelp(ArgumentParser p) {
        p.addArgument("-h", "--help")
         .action(new SafeHelpAction(stdOut))
         .help("show this help message and exit")
         .setDefault(Arguments.SUPPRESS);
    }

    private void addCommand(Command command) {
        commands.put(command.getName(), command);
        parser.addSubparsers().help("available commands");
        final Subparser subparser = parser.addSubparsers().addParser(command.getName(), false);
        command.configure(subparser);
        addHelp(subparser);
        subparser.description(command.getDescription())
                 .setDefault(COMMAND_NAME_ATTR, command.getName())
                 .defaultHelp(true);
    }

    private static class SafeHelpAction implements ArgumentAction {
        private final PrintWriter out;

        SafeHelpAction(PrintWriter out) {
            this.out = out;
        }

        @Override
        public void run(ArgumentParser parser, Argument arg,
                        Map<String, Object> attrs, String flag, Object value)
                throws ArgumentParserException {
            parser.printHelp(out);
            attrs.put("is-help", Boolean.TRUE);
        }

        @Override
        public boolean consumeArgument() {
            return false;
        }

        @Override
        public void onAttach(Argument arg) {
        }
    }
}
