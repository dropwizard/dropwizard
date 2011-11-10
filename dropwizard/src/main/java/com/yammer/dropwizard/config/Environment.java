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
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: 10/12/11 <coda> -- document Environment
// TODO: 10/12/11 <coda> -- test Environment
// TODO: 11/7/11 <coda> -- add support for Jersey params

@NotThreadSafe
public class Environment extends AggregateLifeCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);
    private static final String ROOT_PATH = "/*";

    private final DefaultResourceConfig config;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ImmutableMap.Builder<String, ServletHolder> servlets;
    private final ImmutableMap.Builder<String, FilterHolder> filters;
    private final ImmutableSet.Builder<Task> tasks;

    public Environment() {
        this.config = new DefaultResourceConfig() {
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
                for (Object bean : getBeans()) {
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
        };
        this.healthChecks = ImmutableSet.builder();
        this.servlets = ImmutableMap.builder();
        this.filters = ImmutableMap.builder();
        this.tasks = ImmutableSet.builder();

        config.setPropertiesAndFeatures(ImmutableMap.<String, Object>of(
                ResourceConfig.FEATURE_DISABLE_WADL, true
        ));

        addProvider(new LoggingExceptionMapper());
        addServlet(new ServletContainer(config), ROOT_PATH, Integer.MAX_VALUE);
        addTask(new GarbageCollectionTask());
    }

    public void addResource(@Nonnull Object resource) {
        config.getSingletons().add(checkNotNull(resource));
    }

    public void addResource(@Nonnull Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    public void addProvider(@Nonnull Object provider) {
        config.getSingletons().add(checkNotNull(provider));
    }

    public void addProvider(@Nonnull Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
    }

    public void addHealthCheck(@Nonnull HealthCheck healthCheck) {
        healthChecks.add(checkNotNull(healthCheck));
    }

    public void manage(@Nonnull Managed managed) {
        addBean(new JettyManaged(checkNotNull(managed)));
    }

    public void manage(@Nonnull LifeCycle managed) {
        addBean(checkNotNull(managed));
    }

    public void addServlet(@Nonnull Servlet servlet,
                           @Nonnull String pathSpec) {
        addServlet(checkNotNull(servlet), checkNotNull(pathSpec), 0);
    }

    public void addServlet(@Nonnull Servlet servlet,
                           @Nonnull String pathSpec,
                           int initOrder) {
        addServlet(checkNotNull(servlet), checkNotNull(pathSpec),
                   initOrder, ImmutableMap.<String, String>of());
    }

    public void addServlet(@Nonnull Servlet servlet,
                           @Nonnull String pathSpec,
                           int initOrder,
                           @Nonnull Map<String, String> initParams) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        holder.setInitOrder(initOrder);
        holder.setInitParameters(checkNotNull(initParams));
        servlets.put(checkNotNull(pathSpec), holder);
    }

    public void addServlet(@Nonnull Class<? extends Servlet> klass,
                           @Nonnull String pathSpec) {
        addServlet(checkNotNull(klass), checkNotNull(pathSpec), 0);
    }

    public void addServlet(@Nonnull Class<? extends Servlet> klass,
                           @Nonnull String pathSpec,
                           int initOrder) {
        addServlet(checkNotNull(klass), checkNotNull(pathSpec), initOrder,
                   ImmutableMap.<String, String>of());
    }

    public void addServlet(@Nonnull Class<? extends Servlet> klass,
                           @Nonnull String pathSpec,
                           int initOrder,
                           @Nonnull Map<String, String> initParams) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        holder.setInitOrder(initOrder);
        holder.setInitParameters(checkNotNull(initParams));
        servlets.put(checkNotNull(pathSpec), holder);
    }

    public void addFilter(@Nonnull Filter filter,
                          @Nonnull String pathSpec) {
        addFilter(checkNotNull(filter), checkNotNull(pathSpec),
                  ImmutableMap.<String, String>of());
    }

    public void addFilter(@Nonnull Filter filter,
                          @Nonnull String pathSpec,
                          @Nonnull Map<String, String> initParams) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        holder.setInitParameters(checkNotNull(initParams));
        filters.put(checkNotNull(pathSpec), holder);
    }

    public void addFilter(@Nonnull Class<? extends Filter> klass,
                          @Nonnull String pathSpec) {
        addFilter(checkNotNull(klass), checkNotNull(pathSpec),
                  ImmutableMap.<String, String>of());
    }

    public void addFilter(@Nonnull Class<? extends Filter> klass,
                          @Nonnull String pathSpec,
                          @Nonnull Map<String, String> initParams) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        holder.setInitParameters(checkNotNull(initParams));
        filters.put(checkNotNull(pathSpec), holder);
    }

    public void addTask(@Nonnull Task task) {
        tasks.add(checkNotNull(task));
    }

    public void setJerseyOption(String name, Object value) {
        config.setPropertiesAndFeatures(ImmutableMap.of(name, value));
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
}
