package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.util.JarLocation;
import org.apache.commons.cli.*;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

/**
 * A basic CLI command.
 */
public abstract class Command {
    private final String name;
    private final String description;

    /**
     * Create a new {@link Command} instance.
     *
     * @param name the command name (must be unique for the service)
     * @param description the description of the command
     */
    protected Command(String name,
                      String description) {
        this.name = checkNotNull(name);
        this.description = checkNotNull(description);
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
     * Returns an empty {@link Options} instance. Override this to allow your commands to parse
     * command line arguments.
     *
     * @return an empty {@link Options} instance
     */
    public Options getOptions() {
        return new Options();
    }

    @SuppressWarnings("unchecked")
    final Options getOptionsWithHelp() {
        final Options options = new Options();
        final OptionGroup group = new OptionGroup();
        for (Option option : (Collection<Option>) getOptions().getOptions()) {
            group.addOption(option);
        }
        options.addOptionGroup(group);
        options.addOption("h", "help", false, "display usage information");
        return options;
    }

    /**
     * Executes when the user runs this specific command.
     *
     * @param service the service to which the command belongs
     * @param params the command-line parameters of the invocation
     * @throws Exception if something goes wrong
     */
    protected abstract void run(AbstractService<?> service,
                                CommandLine params) throws Exception;

    /**
     * Returns the usage syntax for the command.
     *
     * @return the usage syntax for the command
     */
    protected String getSyntax() {
        return "[options]";
    }

    /**
     * Returns the usage string for the command, including the JAR file location, if possible.
     *
     * @return the usage string for the command
     */
    protected String getUsage() {
        return format("%s %s %s", new JarLocation(), getName(), getSyntax());
    }

    /**
     * Execute the command.
     *
     * @param service the service to which the command belongs
     * @param arguments the arguments passed to the command
     * @throws Exception if something goes wrong
     */
    public final void run(AbstractService<?> service,
                          String[] arguments) throws Exception {
        final CommandLine cmdLine = new GnuParser().parse(getOptionsWithHelp(), checkNotNull(arguments));
        if (cmdLine.hasOption("help")) {
            printHelp();
        } else {
            run(checkNotNull(service), cmdLine);
        }
    }

    protected final void printHelp() {
        UsagePrinter.printCommandHelp(this);
    }

    protected final void printHelp(String message) {
        UsagePrinter.printCommandHelp(this, message);
    }
}
