package com.yammer.dropwizard;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.cli.UsagePrinter;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;

public abstract class AbstractService<T extends Configuration> {
    static {
        LoggingFactory.bootstrap();
    }

    private final String name;
    private final List<Module> modules;
    private final SortedMap<String, Command> commands;
    private String banner = null;

    protected AbstractService(String name) {
        this.name = name;
        this.modules = Lists.newArrayList();
        this.commands = Maps.newTreeMap();
        addCommand(new ServerCommand<T>(getConfigurationClass()));
    }

    protected abstract void subclassServiceInsteadOfThis();

    public String getName() {
        return name;
    }

    @SuppressWarnings("unchecked")
    public final Class<T> getConfigurationClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public final ImmutableList<Module> getModules() {
        return ImmutableList.copyOf(modules);
    }

    protected final void addModule(Module module) {
        modules.add(module);
    }

    public final ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands.values());
    }

    protected final void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    protected final void addCommand(ConfiguredCommand<T> command) {
        commands.put(command.getName(), command);
    }

    public final boolean hasBanner() {
        return banner != null;
    }

    public final String getBanner() {
        return banner;
    }

    protected final void setBanner(String banner) {
        this.banner = banner;
    }

    public abstract void initialize(T configuration, Environment environment);

    public final void run(String[] arguments) throws Exception {
        if (isHelp(arguments)) {
            UsagePrinter.printRootHelp(this);
        } else {
            final Command cmd = commands.get(arguments[0]);
            if (cmd != null) {
                cmd.run(this, Arrays.copyOfRange(arguments, 1, arguments.length));
            } else {
                UsagePrinter.printRootHelp(this);
            }
        }
    }

    private static boolean isHelp(String[] arguments) {
        return (arguments.length == 0) ||
                ((arguments.length == 1) &&
                        ("-h".equals(arguments[0]) ||
                                "--help".equals(arguments[0])));
    }
}
