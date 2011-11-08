package com.yammer.dropwizard;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.cli.UsagePrinter;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.Environment;

import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

// TODO: 10/12/11 <coda> -- write tests for Service
// TODO: 10/12/11 <coda> -- write docs for Service

public abstract class Service<T extends Configuration> {
    private final Class<T> configurationClass;
    private final String name;
    private final Map<String, Command> commands;

    protected Service(Class<T> configurationClass, String name, Command... commands) {
        this.configurationClass = configurationClass;
        this.name = name;
        this.commands = new TreeMap<String, Command>();

        final ServerCommand<T> serverCommand = new ServerCommand<T>(configurationClass);
        this.commands.put(serverCommand.getName(), serverCommand);

        for (Command command : commands) {
            this.commands.put(command.getName(), command);

        }
    }

    public abstract void configure(T configuration, Environment environment) throws ConfigurationException;

    public final Class<T> getConfigurationClass() {
        return configurationClass;
    }

    public final String getName() {
        return name;
    }

    public Optional<String> getBanner() {
        return Optional.absent();
    }

    public ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands.values());
    }

    public final void run(String[] arguments) throws Exception {
        if (isHelp(arguments)) {
            UsagePrinter.printRootHelp(this);
        } else {
            final Optional<Command> cmd = Optional.fromNullable(commands.get(arguments[0]));
            if (cmd.isPresent()) {
                cmd.get().run(this, Arrays.copyOfRange(arguments, 1, arguments.length));
            } else {
                UsagePrinter.printRootHelp(this);
            }
        }
    }

    private boolean isHelp(String[] arguments) {
        return arguments.length == 0 ||
                (arguments.length == 1 &&
                        (arguments[0].equals("-h") ||
                                arguments[0].equals("--help")));
    }
}

// TODO: 10/11/11 <coda> -- oauth support
