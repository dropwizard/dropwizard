package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.dropwizard.Application;
import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.cli.Command;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;

import java.lang.management.ManagementFactory;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * The pre-start application environment, containing everything required to bootstrap a Dropwizard
 * command.
 *
 * @param <T> the configuration type
 */
public class Bootstrap<T extends Configuration> {
    private final Application<T> application;
    private final ObjectMapper objectMapper;
    private final List<Bundle> bundles;
    private final List<ConfiguredBundle<? super T>> configuredBundles;
    private final List<Command> commands;
    private final MetricRegistry metricRegistry;

    private ConfigurationSourceProvider configurationSourceProvider;
    private ClassLoader classLoader;

    /**
     * Creates a new {@link Bootstrap} for the given application.
     *
     * @param application a Dropwizard {@link Application}
     */
    public Bootstrap(Application<T> application) {
        this.application = application;
        this.objectMapper = Jackson.newObjectMapper();
        this.bundles = Lists.newArrayList();
        this.configuredBundles = Lists.newArrayList();
        this.commands = Lists.newArrayList();
        this.metricRegistry = new MetricRegistry();
        metricRegistry.register("jvm.buffers", new BufferPoolMetricSet(ManagementFactory
                                                                               .getPlatformMBeanServer()));
        metricRegistry.register("jvm.gc", new GarbageCollectorMetricSet());
        metricRegistry.register("jvm.memory", new MemoryUsageGaugeSet());
        metricRegistry.register("jvm.threads", new ThreadStatesGaugeSet());

        this.configurationSourceProvider = new FileConfigurationSourceProvider();
        this.classLoader = Thread.currentThread().getContextClassLoader();
    }

    /**
     * Returns the bootstrap's {@link Application}.
     */
    public Application<T> getApplication() {
        return application;
    }

    /**
     * Returns the bootstrap's {@link ConfigurationSourceProvider}.
     */
    public ConfigurationSourceProvider getConfigurationSourceProvider() {
        return configurationSourceProvider;
    }

    /**
     * Sets the bootstrap's {@link ConfigurationSourceProvider}.
     */
    public void setConfigurationSourceProvider(ConfigurationSourceProvider provider) {
        this.configurationSourceProvider = checkNotNull(provider);
    }

    /**
     * Returns the bootstrap's class loader.
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the bootstrap's class loader.
     */
    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Adds the given bundle to the bootstrap.
     *
     * @param bundle a {@link Bundle}
     */
    public void addBundle(Bundle bundle) {
        bundle.initialize(this);
        bundles.add(bundle);
    }

    /**
     * Adds the given bundle to the bootstrap.
     *
     * @param bundle a {@link ConfiguredBundle}
     */
    public void addBundle(ConfiguredBundle<? super T> bundle) {
        bundle.initialize(this);
        configuredBundles.add(bundle);
    }

    /**
     * Adds the given command to the bootstrap.
     *
     * @param command a {@link Command}
     */
    public void addCommand(Command command) {
        commands.add(command);
    }

    /**
     * Adds the given command to the bootstrap.
     *
     * @param command a {@link ConfiguredCommand}
     */
    public void addCommand(ConfiguredCommand<T> command) {
        commands.add(command);
    }

    /**
     * Returns the bootstrap's {@link ObjectMapper}.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Runs the bootstrap's bundles with the given configuration and environment.
     *
     * @param configuration the parsed configuration
     * @param environment   the application environment
     * @throws Exception if a bundle throws an exception
     */
    public void run(T configuration, Environment environment) throws Exception {
        for (Bundle bundle : bundles) {
            bundle.run(environment);
        }
        for (ConfiguredBundle<? super T> bundle : configuredBundles) {
            bundle.run(configuration, environment);
        }
    }

    /**
     * Returns the application's commands.
     */
    public ImmutableList<Command> getCommands() {
        return ImmutableList.copyOf(commands);
    }

    /**
     * Returns the application's metrics.
     */
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }
}
