package com.yammer.dropwizard.config;

import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.jetty.JettyManaged;
import com.yammer.dropwizard.jetty.NonblockingServletHolder;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;

import java.util.EventListener;
import java.util.concurrent.*;

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
    private static final Log LOG = Log.forClass(Environment.class);

    private final AbstractService<?> service;
    private final DropwizardResourceConfig config;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ImmutableMap.Builder<String, ServletHolder> servlets;
    private final ImmutableMultimap.Builder<String, FilterHolder> filters;
    private final ImmutableSet.Builder<EventListener> servletListeners;
    private final ImmutableSet.Builder<Task> tasks;
    private final AggregateLifeCycle lifeCycle;

    /**
     * Creates a new environment.
     * 
     * @param configuration    the service's {@link Configuration}
     * @param service          the service
     */
    public Environment(Configuration configuration, AbstractService<?> service) {
        this.service = service;
        this.config = new DropwizardResourceConfig() {
            @Override
            public void validate() {
                super.validate();
                logResources();
                logProviders();
                logHealthChecks();
                logManagedObjects();
                logEndpoints();
            }
        };
        this.healthChecks = ImmutableSet.builder();
        this.servlets = ImmutableMap.builder();
        this.filters = ImmutableMultimap.builder();
        this.servletListeners = ImmutableSet.builder();
        this.tasks = ImmutableSet.builder();
        this.lifeCycle = new AggregateLifeCycle();
        
        HttpServlet jerseyContainer = service.getJerseyContainer(config);
        if (jerseyContainer != null) {
            addServlet(jerseyContainer, configuration.getHttpConfiguration().getRootPath()).setInitOrder(Integer.MAX_VALUE);
        }
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
     * Add one or more servlet event listeners.
     * 
     * @param listeners one or more listener instances that implement
     *                  {@link javax.servlet.ServletContextListener}, 
     *                  {@link javax.servlet.ServletContextAttributeListener}, 
     *                  {@link javax.servlet.ServletRequestListener} or 
     *                  {@link javax.servlet.ServletRequestAttributeListener}
     * 
     */
    public void addServletListeners(EventListener... listeners) {
        this.servletListeners.add( listeners );
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

    /**
     * Creates a new {@link ExecutorService} instance with the given parameters whose lifecycle is
     * managed by the service.
     *
     * @param nameFormat               a {@link String#format(String, Object...)}-compatible format
     *                                 String, to which a unique integer (0, 1, etc.) will be
     *                                 supplied as the single parameter.
     * @param corePoolSize             the number of threads to keep in the pool, even if they are
     *                                 idle.
     * @param maximumPoolSize          the maximum number of threads to allow in the pool.
     * @param keepAliveTime            when the number of threads is greater than the core, this is
     *                                 the maximum time that excess idle threads will wait for new
     *                                 tasks before terminating.
     * @param unit                     the time unit for the keepAliveTime argument.
     *
     * @return a new {@link ExecutorService} instance
     */
    public ExecutorService managedExecutorService(String nameFormat,
                                                  int corePoolSize,
                                                  int maximumPoolSize,
                                                  long keepAliveTime,
                                                  TimeUnit unit) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat)
                                                                      .build();
        final ExecutorService executor = new ThreadPoolExecutor(corePoolSize,
                                                                maximumPoolSize,
                                                                keepAliveTime,
                                                                unit,
                                                                new LinkedBlockingQueue<Runnable>(),
                                                                threadFactory);
        manage(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS));
        return executor;
    }

    /**
     * Creates a new {@link ScheduledExecutorService} instance with the given parameters whose
     * lifecycle is managed by the service.
     *
     * @param nameFormat               a {@link String#format(String, Object...)}-compatible format
     *                                 String, to which a unique integer (0, 1, etc.) will be
     *                                 supplied as the single parameter.
     * @param corePoolSize             the number of threads to keep in the pool, even if they are
     *                                 idle.
     *
     * @return a new {@link ScheduledExecutorService} instance
     */
    public ScheduledExecutorService managedScheduledExecutorService(String nameFormat,
                                                                    int corePoolSize) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(nameFormat)
                                                                      .build();
        final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(corePoolSize,
                                                                                  threadFactory);
        manage(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS));
        return executor;
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

    ImmutableMultimap<String, FilterHolder> getFilters() {
        return filters.build();
    }

    ImmutableSet<Task> getTasks() {
        return tasks.build();
    }

    ImmutableSet<EventListener> getServletListeners() {
        return servletListeners.build();
    }
    
    private void logManagedObjects() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (Object bean : lifeCycle.getBeans()) {
            builder.add(bean.getClass().getCanonicalName());
        }
        LOG.debug("managed objects = {}", builder.build());
    }

    private void logHealthChecks() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (HealthCheck healthCheck : healthChecks.build()) {
            builder.add(healthCheck.getClass().getCanonicalName());
        }
        LOG.debug("health checks = {}", builder.build());
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

        LOG.debug("resources = {}", builder.build());
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

        LOG.debug("providers = {}", builder.build());
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

        LOG.info(stringBuilder.toString());
    }

    private MethodList annotatedMethods(Class<?> resource) {
        return new MethodList(resource, true).hasMetaAnnotation(HttpMethod.class);
    }

    public AbstractService<?> getService() {
        return service;
    }
}
