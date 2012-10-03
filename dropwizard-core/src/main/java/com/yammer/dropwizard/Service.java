package com.yammer.dropwizard;

import com.fasterxml.jackson.databind.Module;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.bundles.BasicBundle;
import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.cli.Command;
import com.yammer.dropwizard.cli.ConfiguredCommand;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.json.Json;

import javax.annotation.CheckForNull;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * The base class for both Java and Scala services. Do not extend this directly. Use {@link Service}
 * instead.
 *
 * @param <T>    the type of configuration class for this service
 */
@SuppressWarnings("EmptyMethod")
public abstract class Service<T extends Configuration> {
    static {
        // make sure spinning up Hibernate Validator doesn't yell at us
        LoggingFactory.bootstrap();
    }

    private final String name;
    private final List<Bundle> bundles;
    private final List<ConfiguredBundle<? super T>> configuredBundles;
    private final List<Module> modules;
    private final List<Command> commands;

    /**
     * Creates a new service with the given name. If name is {@code null} the service is named as
     * the subclass by using {@link Class#getSimpleName()}.
     *
     * @param name    the service's name
     */
    protected Service(String name) {
        this.name = (name == null) ? getClass().getSimpleName() : name;
        this.bundles = Lists.newArrayList();
        this.configuredBundles = Lists.newArrayList();
        this.modules = Lists.newArrayList();
        this.commands = Lists.newArrayList();
        addCommand(new ServerCommand<T>(getConfigurationClass()));
        addBundle(new BasicBundle(this));
    }

    protected Service() {
        this(null);
    }

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
        Type t = getClass();
        while (t instanceof Class<?>) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        // Similar to [Issue-89] (see {@link com.yammer.dropwizard.cli.ConfiguredCommand#getConfigurationClass})
        if (t instanceof ParameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (Type param : ((ParameterizedType) t).getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    final Class<?> cls = (Class<?>) param;
                    if (Configuration.class.isAssignableFrom(cls)) {
                        return (Class<T>) cls;
                    }
                }
            }
        }
        throw new IllegalStateException("Can not figure out Configuration type parameterization for "+getClass().getName());
    }

    /**
     * Registers a {@link Bundle} to be used in initializing the service's {@link Environment}.
     *
     * @param bundle    a bundle
     * @see Bundle
     */
    protected final void addBundle(Bundle bundle) {
        bundles.add(bundle);
    }

    /**
     * Registers a {@link ConfiguredBundle} to be used in initializing the service's
     * {@link Environment}.
     *
     * @param bundle a bundle
     * @see ConfiguredBundle
     */
    protected final void addBundle(ConfiguredBundle<? super T> bundle) {
        configuredBundles.add(bundle);
    }

    /**
     * Returns a list of registered {@link Command} instances.
     *
     * @return a list of commands
     */
    public final ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    /**
     * Registers a {@link Command} which the service will provide.
     *
     * @param command    a command
     */
    protected final void addCommand(Command command) {
        commands.add(command);
    }

    /**
     * Registers a {@link ConfiguredCommand} which the service will provide.
     *
     * @param command    a command
     */
    protected final void addCommand(ConfiguredCommand<T> command) {
        commands.add(command);
    }

    /**
     * Registers a Jackson {@link Module} which the service will use to parse the configuration file
     * and to parse/generate any {@code application/json} entities.
     *
     * @param module    a {@link Module}
     */
    protected final void addJacksonModule(Module module) {
        modules.add(module);
    }

    /**
     * When the service runs, this is called after the {@link Bundle}s are run. Override it to add
     * providers, resources, etc. for your service.
     *
     * @param configuration    the parsed {@link Configuration} object
     * @param environment      the service's {@link Environment}
     * @throws Exception if something goes wrong
     */
    protected abstract void initialize(T configuration, Environment environment) throws Exception;

    /**
     * Initializes the given {@link Environment} given a {@link Configuration} instances. First the
     * bundles are initialized in the order they were added, then the service's
     * {@link #initialize(Configuration, Environment)} method is called.
     *
     * @param configuration    the parsed {@link Configuration} object
     * @param environment      the service's {@link Environment}
     * @throws Exception if something goes wrong
     */
    public final void initializeWithBundles(T configuration, Environment environment) throws Exception {
        for (Bundle bundle : bundles) {
            bundle.initialize(environment);
        }
        for (ConfiguredBundle<? super T> bundle : configuredBundles) {
            bundle.initialize(configuration, environment);
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
        final Cli cli = new Cli(this, commands, System.err);
        cli.runAndExit(arguments);
    }

    /**
     * Returns a list of Jackson {@link Module}s used to parse JSON (and the configuration files).
     *
     * @return a list of {@link Module}s
     */
    public ImmutableList<Module> getJacksonModules() {
        return ImmutableList.copyOf(modules);
    }
    
    /**
     * Returns the json environment wrapper that configures and calls into jackson
     * @return an instanceof Json
     */
    public Json getJson() {
        final Json json = new Json();
        for (Module module : getJacksonModules()) {
            json.registerModule(module);
        }
        return json;
    }

    /**
     * Returns the Jersey servlet container used to serve HTTP requests. This implementation
     * creates a new {@link ServletContainer} instance with the given resource configuration.
     * Subclasses must either use the same {@code config} instance or delegate to it.
     * <p>
     * This method may be called before the service initialized with 
     * {@link #initialize(Configuration, Environment)}; service implementations must not 
     * assume the service has been initialized.
     * <p>
     * An implementation that chooses to return {@code null} is responsible for creating
     * a container with the given config by other means during initialization and startup.
     * 
     *
     * @param resourceConfig    the Jersey resource config to use for the container
     * @param serviceConfig     the service configuration object
     * @return a Jersey servlet container, or {@code null} if the Jersey container
     *         will be created by other means 
     */
    @CheckForNull
    public ServletContainer getJerseyContainer(DropwizardResourceConfig resourceConfig,
                                               T serviceConfig) {
        return new ServletContainer(resourceConfig);
    }
}
