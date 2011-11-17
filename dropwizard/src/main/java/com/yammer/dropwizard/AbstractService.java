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

/**
 * The base class for both Java and Scala services. Do not extend this directly. Use {@link Service}
 * instead.
 *
 * @param <T>    the type of configuration class for this service
 */
@SuppressWarnings("EmptyMethod")
public abstract class AbstractService<T extends Configuration> {
    static {
        // make sure spinning up Hibernate Validator doesn't yell at us
        LoggingFactory.bootstrap();
    }

    private final String name;
    private final List<Module> modules;
    private final List<ConfiguredModule<? super T>> configuredModules;
    private final SortedMap<String, Command> commands;
    private String banner = null;

    /**
     * Creates a new service with the given name.
     *
     * @param name    the service's name
     */
    protected AbstractService(String name) {
        this.name = name;
        this.modules = Lists.newArrayList();
        this.configuredModules = Lists.newArrayList();
        this.commands = Maps.newTreeMap();
        addCommand(new ServerCommand<T>(getConfigurationClass()));
    }

    /**
     * A simple reminder that this particular class isn't meant to be extended by non-DW classes.
     */
    protected abstract void subclassServiceInsteadOfThis();

    public final String getName() {
        return name;
    }

    /**
     * Returns the {@link Class} of the configuration class type parameter.
     *
     * @return the configuration class
     * @see <a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html">Super Type Tokens</a>
     */
    @SuppressWarnings("unchecked")
    public final Class<T> getConfigurationClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Registers a {@link Module} to be used in initializing the service's {@link Environment}.
     *
     * @param module    a module
     * @see Module
     */
    protected final void addModule(Module module) {
        modules.add(module);
    }

    /**
     * Registers a {@link ConfiguredModule} to be used in initializing the service's
     * {@link Environment}.
     *
     * @param module a module
     * @see ConfiguredModule
     */
    protected final void addModule(ConfiguredModule<? super T> module) {
        configuredModules.add(module);
    }

    /**
     * Returns a list of registered {@link Command} instances.
     *
     * @return a list of commands
     */
    public final ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands.values());
    }

    /**
     * Registers a {@link Command} which the service will provide.
     *
     * @param command    a command
     */
    protected final void addCommand(Command command) {
        commands.put(command.getName(), command);
    }

    /**
     * Registers a {@link ConfiguredCommand} which the service will provide.
     *
     * @param command    a command
     */
    protected final void addCommand(ConfiguredCommand<T> command) {
        commands.put(command.getName(), command);
    }

    /**
     * Returns {@code true} if the service has a banner.
     *
     * @return whether or not the service has a banner
     */
    public final boolean hasBanner() {
        return banner != null;
    }

    /**
     * Returns the service's banner, if any. The banner will be printed out when the service starts
     * up.
     *
     * @return the service's banner
     */
    public final String getBanner() {
        return banner;
    }

    /**
     * Sets the service's banner. The banner will be printed out when the service starts up.
     *
     * @param banner    a banner
     */
    protected final void setBanner(String banner) {
        this.banner = banner;
    }

    /**
     * When the service runs, this is called after the {@link Module}s are run. Override it to add
     * providers, resources, etc. for your service.
     *
     * @param configuration    the parsed {@link Configuration} object
     * @param environment      the service's {@link Environment}
     */
    protected abstract void initialize(T configuration, Environment environment) throws Exception;

    /**
     * Initializes the given {@link Environment} given a {@link Configuration} instances. First the
     * modules are initialized in the order they were added, then the service's
     * {@link #initialize(Configuration, Environment)} method is called.
     *
     * @param configuration    the parsed {@link Configuration} object
     * @param environment      the service's {@link Environment}
     */
    public final void initializeWithModules(T configuration, Environment environment) throws Exception {
        for (Module module : modules) {
            module.initialize(environment);
        }
        for (ConfiguredModule<? super T> module : configuredModules) {
            module.initialize(configuration, environment);
        }
        initialize(configuration, environment);
    }

    /**
     * Parses command-line arguments and runs the service. Call this method from a
     * {@code public static void main} entry point in your application.
     *
     * @param arguments    the command-line arguments
     * @throws Exception if something goes wrong
     */
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
