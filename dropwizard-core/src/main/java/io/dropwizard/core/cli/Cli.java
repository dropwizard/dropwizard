package io.dropwizard.core.cli;

import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.util.JarLocation;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.helper.HelpScreenException;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

import static java.util.Objects.requireNonNull;

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
     * Create a new CLI interface for an application and its bootstrapped environment.
     *
     * @param location     the location of the application
     * @param bootstrap    the bootstrap for the application
     * @param stdOut       standard out
     * @param stdErr       standard err
     */
    public Cli(JarLocation location, Bootstrap<?> bootstrap, OutputStream stdOut, OutputStream stdErr) {
        this.stdOut = new PrintWriter(new OutputStreamWriter(stdOut, StandardCharsets.UTF_8), true);
        this.stdErr = new PrintWriter(new OutputStreamWriter(stdErr, StandardCharsets.UTF_8), true);
        this.commands = new TreeMap<>();
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
     * @return the error or an empty optional if command succeeded
     */
    public Optional<Throwable> run(String... arguments) {
        try {
            if (isFlag(HELP, arguments)) {
                parser.printHelp(stdOut);
            } else if (isFlag(VERSION, arguments)) {
                parser.printVersion(stdOut);
            } else {
                final Namespace namespace = parser.parseArgs(arguments);
                final Command command = requireNonNull(commands.get(namespace.getString(COMMAND_NAME_ATTR)),
                    "Command is not found");
                try {
                    command.run(bootstrap, namespace);
                } catch (Throwable e) {
                    // The command failed to run, and the command knows
                    // best how to clean up / debug exception
                    command.onError(this, namespace, e);
                    return Optional.of(e);
                }
            }
            return Optional.empty();
        } catch (HelpScreenException ignored) {
            // This exception is triggered when the user passes in a help flag.
            // Return true to signal that the process executed normally.
            return Optional.empty();
        } catch (ArgumentParserException e) {
            stdErr.println(e.getMessage());
            e.getParser().printHelp(stdErr);
            return Optional.of(e);
        }
    }

    private static boolean isFlag(String[][] flags, String[] arguments) {
        for (String[] cmd : flags) {
            if (Arrays.equals(arguments, cmd)) {
                return true;
            }
        }
        return false;
    }

    private ArgumentParser buildParser(JarLocation location) {
        final String usage = "java -jar " + location;
        final ArgumentParser p = ArgumentParsers.newFor(usage).addHelp(false).build();
        p.version(location.getVersion().orElse(
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

    public PrintWriter getStdOut() {
        return stdOut;
    }

    public PrintWriter getStdErr() {
        return stdErr;
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
            throw new HelpScreenException(parser);
        }

        @Override
        public boolean consumeArgument() {
            return false;
        }

        @Override
        public void onAttach(Argument arg) {
            // Nothing to do
        }
    }
}
