package com.yammer.dropwizard.cli;

import com.beust.jcommander.JCommander;
import com.google.common.base.Optional;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.util.JarLocation;

import java.io.PrintStream;

public class Cli {
    private final Bootstrap<?> bootstrap;
    private final PrintStream output;
    private final JCommander commander;
    private final HelpCommand helpCommand;

    public Cli(Bootstrap<?> bootstrap,
               Service<?> service,
               PrintStream output) {
        this.bootstrap = bootstrap;
        this.output = output;
        this.commander = new JCommander();
        this.helpCommand = new HelpCommand(commander, output);
        commander.addCommand(helpCommand);
        commander.setProgramName("java -jar " + new JarLocation(service.getClass()));
        for (Command command : bootstrap.getCommands()) {
            commander.addCommand(command);
        }
    }

    public Optional<String> run(String[] arguments) throws Exception {
        try {
            commander.parse(arguments);
            final String commandName = commander.getParsedCommand();
            if (commandName == null) {
                helpCommand.run(bootstrap);
                return Optional.of("");
            }
            final Command command = (Command) commander.getCommands()
                                                       .get(commandName)
                                                       .getObjects()
                                                       .get(0);
            command.run(bootstrap);
            return Optional.absent();
        } catch (Exception e) {
            if (e.getMessage() == null) {
                return Optional.of("A " + e.getClass().getName() + " was thrown.");
            }
            return Optional.of(e.getMessage());
        }
    }

    @SuppressWarnings("UseOfSystemOutOrSystemErr")
    public void runAndExit(String[] arguments) throws Exception {
        final Optional<String> result = run(arguments);
        if (result.isPresent()) {
            output.println(result.get());
            System.exit(1);
        }
    }
}
