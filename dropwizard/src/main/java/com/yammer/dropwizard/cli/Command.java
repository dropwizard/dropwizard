package com.yammer.dropwizard.cli;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.util.JarLocation;
import org.apache.commons.cli.*;

import java.util.Collection;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.String.format;

// TODO: 10/12/11 <coda> -- write tests for Command
// TODO: 10/12/11 <coda> -- write docs for Command

public abstract class Command {
    private final String name;
    private final Optional<String> description;

    protected Command(String name,
                      String description) {
        this.name = checkNotNull(name);
        this.description = Optional.fromNullable(description);
    }

    protected Command(String name) {
        this.name = checkNotNull(name);
        this.description = Optional.absent();
    }

    public final String getName() {
        return name;
    }

    public final Optional<String> getDescription() {
        return description;
    }

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

    protected abstract void run(AbstractService<?> service,
                                CommandLine params) throws Exception;

    protected String getSyntax() {
        return "[options]";
    }

    protected String getUsage() {
        return format("%s %s %s", new JarLocation(), getName(), getSyntax());
    }

    public final void run(AbstractService<?> service,
                          String[] arguments) throws Exception {
        final CommandLine cmdLine = new GnuParser().parse(getOptionsWithHelp(), checkNotNull(arguments));
        if (cmdLine.hasOption("help")) {
            UsagePrinter.printCommandHelp(this);
        } else {
            run(Preconditions.checkNotNull(service), cmdLine);
        }
    }
}
