package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.LoggingExceptionMapper;
import com.yammer.dropwizard.jetty.JettyManaged;
import com.yammer.dropwizard.jetty.NonblockingServletHolder;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: 10/12/11 <coda> -- test Environment
/*
    REVIEW: 11/12/11 <coda> -- Probably better to invert this code.
    Instead of letting it collect intermediate results and then exposing those via package-private
    getters, it might be better to pass this a ServletContextHandler, etc., and have it modify
    those directly. That's easier to test.
*/

/**
 * A Dropwizard service's environment.
 */
public class Environment extends AbstractLifeCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);
    private static final String ROOT_PATH = "/*";

    private final ResourceConfig config;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ImmutableMap.Builder<String, ServletHolder> servlets;
    private final ImmutableMap.Builder<String, FilterHolder> filters;
    private final ImmutableSet.Builder<Task> tasks;
    private final AggregateLifeCycle lifeCycle;

    /**
     * Creates a new environment.
     */
    public Environment() {
        this.config = new DropwizardResourceConfig();
        this.healthChecks = ImmutableSet.builder();
        this.servlets = ImmutableMap.builder();
        this.filters = ImmutableMap.builder();
        this.tasks = ImmutableSet.builder();
        this.lifeCycle = new AggregateLifeCycle();

        enableJerseyFeature(ResourceConfig.FEATURE_DISABLE_WADL);
        addProvider(new LoggingExceptionMapper());
        addServlet(new ServletContainer(config), ROOT_PATH).setInitOrder(Integer.MAX_VALUE);
        addTask(new GarbageCollectionTask());
    }

    @Override
    protected void doStart() throws Exception {
        lifeCycle.start();
    }

    @Override
    protected void doStop() throws Exception {
        lifeCycle.stop();
    }

    /**
     * Adds the given object as a Jersey singleton resource.
     *
     * @param resource    a Jersey singleton resource
     */
    public void addResource(Object resource) {
        config.getSingletons().add(checkNotNull(resource));
    }

    /**
     * Adds the given class as a Jersey resource.
     * <p/><b>N.B.:</b> This class must either have a no-args constructor or use Jersey's built-in
     * dependency injection.
     *
     * @param klass    a Jersey resource class
     */
    public void addResource(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    /**
     * Adds the given object as a Jersey provider.
     *
     * @param provider    a Jersey provider
     */
    public void addProvider(Object provider) {
        config.getSingletons().add(checkNotNull(provider));
    }

    /**
     * Adds the given class as a Jersey provider.
     * <p/><b>N.B.:</b> This class must either have a no-args constructor or use Jersey's built-in
     * dependency injection.
     *
     * @param klass    a Jersey provider class
     */
    public void addProvider(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    /**
     * Adds the given health check to the set of health checks exposed on the admin port.
     *
     * @param healthCheck    a health check
     */
    public void addHealthCheck(HealthCheck healthCheck) {
        healthChecks.add(checkNotNull(healthCheck));
    }

    /**
     * Adds the given {@link Managed} instance to the set of objects managed by the server's
     * lifecycle. When the server starts, {@code managed} will be started. When the server stops,
     * {@code managed} will be stopped.
     *
     * @param managed    a managed object
     */
    public void manage(Managed managed) {
        lifeCycle.addBean(new JettyManaged(checkNotNull(managed)));
    }

    /**
     * Adds the given Jetty {@link LifeCycle} instances to the server's lifecycle.
     *
     * @param managed    a Jetty-managed object
     */
    public void manage(LifeCycle managed) {
        lifeCycle.addBean(checkNotNull(managed));
    }

    /**
     * Add a servlet instance.
     *
     * @param servlet       the servlet instance
     * @param urlPattern    the URL pattern for requests that should be handled by {@code servlet}
     * @return a {@link ServletConfiguration} instance allowing for further configuration
     */
    public ServletConfiguration addServlet(Servlet servlet,
                                           String urlPattern) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        final ServletConfiguration configuration = new ServletConfiguration(holder, servlets);
        configuration.addUrlPattern(checkNotNull(urlPattern));
        return configuration;
    }

    /**
     * Add a servlet class.
     *
     * @param klass         the servlet class
     * @param urlPattern    the URL pattern for requests that should be handled by instances of
     *                      {@code klass}
     * @return a {@link ServletConfiguration} instance allowing for further configuration
     */
    public ServletConfiguration addServlet(Class<? extends Servlet> klass,
                                           String urlPattern) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        final ServletConfiguration configuration = new ServletConfiguration(holder, servlets);
        configuration.addUrlPattern(checkNotNull(urlPattern));
        return configuration;
    }

    /**
     * Add a filter instance.
     *
     * @param filter        the filter instance
     * @param urlPattern    the URL pattern for requests that should be handled by {@code filter}
     * @return a {@link FilterConfiguration} instance allowing for further configuration
     */
    public FilterConfiguration addFilter(Filter filter,
                                         String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        final FilterConfiguration configuration = new FilterConfiguration(holder, filters);
        configuration.addUrlPattern(checkNotNull(urlPattern));
        return configuration;
    }

    /**
     * Add a filter class.
     *
     * @param klass         the filter class
     * @param urlPattern    the URL pattern for requests that should be handled by instances of
     *                      {@code klass}
     * @return a {@link FilterConfiguration} instance allowing for further configuration
     */
    public FilterConfiguration addFilter(Class<? extends Filter> klass,
                                         String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        final FilterConfiguration configuration = new FilterConfiguration(holder, filters);
        configuration.addUrlPattern(checkNotNull(urlPattern));
        return configuration;
    }

    /**
     * Adds a {@link Task} instance.
     *
     * @param task    a {@link Task}
     */
    public void addTask(Task task) {
        tasks.add(checkNotNull(task));
    }

    /**
     * Enables the Jersey feature with the given name.
     *
     * @param name    the name of the feature to be enabled
     * @see ResourceConfig
     */
    public void enableJerseyFeature(String name) {
        config.getFeatures().put(checkNotNull(name), Boolean.TRUE);
    }

    /**
     * Disables the Jersey feature with the given name.
     *
     * @param name    the name of the feature to be disabled
     * @see ResourceConfig
     */
    public void disableJerseyFeature(String name) {
        config.getFeatures().put(checkNotNull(name), Boolean.FALSE);
    }

    /**
     * Sets the given Jersey property.
     *
     * @param name     the name of the Jersey property
     * @param value    the value of the Jersey property
     * @see ResourceConfig
     */
    public void setJerseyProperty(String name,
                                  @Nullable Object value) {
        config.getProperties().put(checkNotNull(name), value);
    }

    /*
     * Internal Accessors
     */

    ImmutableSet<HealthCheck> getHealthChecks() {
        return healthChecks.build();
    }

    ImmutableMap<String, ServletHolder> getServlets() {
        return servlets.build();
    }

    ImmutableMap<String, FilterHolder> getFilters() {
        return filters.build();
    }

    ImmutableSet<Task> getTasks() {
        return tasks.build();
    }

    private class DropwizardResourceConfig extends DefaultResourceConfig {
        @Override
        public void validate() {
            super.validate();
            logResources();
            logProviders();
            logHealthChecks();
            logManagedObjects();
            logEndpoints();
        }

        private void logManagedObjects() {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (Object bean : lifeCycle.getBeans()) {
                builder.add(bean.getClass().getCanonicalName());
            }
            LOGGER.debug("managed objects = {}", builder.build());
        }

        private void logHealthChecks() {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
            for (HealthCheck healthCheck : healthChecks.build()) {
                builder.add(healthCheck.getClass().getCanonicalName());
            }
            LOGGER.debug("health checks = {}", builder.build());
        }

        private void logResources() {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

            for (Class<?> klass : config.getClasses()) {
                if (klass.isAnnotationPresent(Path.class)) {
                    builder.add(klass.getCanonicalName());
                }
            }

            for (Object o : config.getSingletons()) {
                if (o.getClass().isAnnotationPresent(Path.class)) {
                    builder.add(o.getClass().getCanonicalName());
                }
            }

            LOGGER.debug("resources = {}", builder.build());
        }

        private void logProviders() {
            final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

            for (Class<?> klass : config.getClasses()) {
                if (klass.isAnnotationPresent(Provider.class)) {
                    builder.add(klass.getCanonicalName());
                }
            }

            for (Object o : config.getSingletons()) {
                if (o.getClass().isAnnotationPresent(Provider.class)) {
                    builder.add(o.getClass().getCanonicalName());
                }
            }

            LOGGER.debug("providers = {}", builder.build());
        }

        private void logEndpoints() {
            final StringBuilder stringBuilder = new StringBuilder(1024).append("\n\n");

            final ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
            for (Object o : config.getSingletons()) {
                if (o.getClass().isAnnotationPresent(Path.class)) {
                    builder.add(o.getClass());
                }
            }
            for (Class<?> klass : config.getClasses()) {
                if (klass.isAnnotationPresent(Path.class)) {
                    builder.add(klass);
                }
            }

            for (Class<?> klass : builder.build()) {
                final String path = klass.getAnnotation(Path.class).value();
                final ImmutableList.Builder<String> endpoints = ImmutableList.builder();
                for (AnnotatedMethod method : annotatedMethods(klass)) {
                    for (HttpMethod verb : method.getMetaMethodAnnotations(HttpMethod.class)) {
                        endpoints.add(String.format("    %-7s %s (%s)",
                                                    verb.value(),
                                                    path,
                                                    klass.getCanonicalName()));
                    }
                }

                for (String line : Ordering.natural()
                                           .sortedCopy(endpoints.build())) {
                    stringBuilder.append(line).append('\n');
                }
            }

            LOGGER.info(stringBuilder.toString());
        }

        private MethodList annotatedMethods(Class<?> resource) {
            return new MethodList(resource, true).hasMetaAnnotation(HttpMethod.class);
        }
    }
}
