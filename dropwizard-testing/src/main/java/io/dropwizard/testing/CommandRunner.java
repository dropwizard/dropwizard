package io.dropwizard.testing;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.Command;

/**
 * A test support class to initialize the application and run a command.
 * <p>
 * By default, the {@link Application} will be constructed using reflection to invoke the nullary
 * constructor. If your application does not provide a public nullary constructor, you will need to
 * override the {@link #newApplication()} method to provide your application instance(s).
 * </p>
 * @param <C> the configuration type
 */
public class CommandRunner<C extends Configuration> extends CommandHelper<C> {
    protected final String commandName;

    protected Command command;

    public CommandRunner(Class<? extends Application<C>> applicationClass,
                         String configPath,
                         String commandName,
                         ConfigOverride... configOverrides) {
        super(applicationClass, configPath, configOverrides);
        this.commandName = commandName;
        this.command = null;
    }

    public CommandRunner(Class<? extends Application<C>> applicationClass,
                         String configPath,
                         Command command,
                         ConfigOverride... configOverrides) {
        super(applicationClass, configPath, configOverrides);
        this.commandName = null;
        this.command = command;
    }

    protected Command getCommand(String name) {
        if(bootstrap == null) throw new RuntimeException("Must be initialized before getCommand is called.");
        for(Command command : bootstrap.getCommands()) {
            if(command.getName().equals(name)) return command;
        }
        return null;
    }

    /**
     * Initialize the application and run the specified command.
     */
    public void run() {
        applyConfigOverrides();
        initialize();

        try {
            if (command == null) {
                if(commandName == null) throw new RuntimeException("Either the command or the commandName must be set.");
                command = getCommand(commandName);
            }
            command.run(bootstrap, namespace);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        resetConfigOverrides();
    }
}
