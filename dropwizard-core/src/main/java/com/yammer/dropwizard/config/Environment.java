package com.yammer.dropwizard.config;

import com.google.common.collect.*;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jetty.JettyManaged;
import com.yammer.dropwizard.jetty.NonblockingServletHolder;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.servlet.Filter;
import javax.servlet.Servlet;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    private final String name;
    private final Configuration configuration;
    private final DropwizardResourceConfig config;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ImmutableMap.Builder<String, ServletHolder> servlets;
    private final ImmutableMultimap.Builder<String, FilterHolder> filters;
    private final ImmutableSet.Builder<EventListener> servletListeners;
    private final ImmutableSet.Builder<Task> tasks;
    private final ImmutableSet.Builder<String> protectedTargets;
    private Resource baseResource;
    private final AggregateLifeCycle lifeCycle;
    private final ObjectMapperFactory objectMapperFactory;
    private SessionHandler sessionHandler;
    private ServletContainer jerseyServletContainer;


    /**
     * Creates a new environment.
     *
     * @param name                the name of the service
     * @param configuration       the service's {@link Configuration}
     * @param objectMapperFactory the {@link ObjectMapperFactory} for the service
     */
    public Environment(String name,
                       Configuration configuration,
                       ObjectMapperFactory objectMapperFactory) {
        this.name = name;
        this.configuration = configuration;
        this.objectMapperFactory = objectMapperFactory;
        this.config = new DropwizardResourceConfig(false) {
            @Override
            public void validate() {
                super.validate();
                logResources();
                logProviders();
                logHealthChecks();
                logManagedObjects();
                logEndpoints();
                logTasks();
            }
        };
        this.healthChecks = ImmutableSet.builder();
        this.servlets = ImmutableMap.builder();
        this.filters = ImmutableMultimap.builder();
        this.servletListeners = ImmutableSet.builder();
        this.tasks = ImmutableSet.builder();
        this.baseResource = Resource.newClassPathResource(".");
        this.protectedTargets = ImmutableSet.builder();
        this.lifeCycle = new AggregateLifeCycle();
        this.jerseyServletContainer = new ServletContainer(config);
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
     * Scans the packages and sub-packages of the given {@link Class} objects for resources and
     * providers.
     *  
     * @param classes     the classes whose packages to scan
     */
    public void scanPackagesForResourcesAndProviders(Class<?>... classes) {
        checkNotNull(classes);
        final String[] names = new String[classes.length];
        for(int i = 0; i < classes.length; i++) {
            names[i] = classes[i].getPackage().getName();
        }
        config.init(new PackageNamesScanner(names));
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
     * @return a {@link ServletBuilder} instance allowing for further configuration
     */
    public ServletBuilder addServlet(Servlet servlet,
                                           String urlPattern) {
        final ServletHolder holder = new NonblockingServletHolder(checkNotNull(servlet));
        final ServletBuilder servletConfig = new ServletBuilder(holder, servlets);
        servletConfig.addUrlPattern(checkNotNull(urlPattern));
        return servletConfig;
    }

    /**
     * Add a servlet class.
     *
     * @param klass         the servlet class
     * @param urlPattern    the URL pattern for requests that should be handled by instances of
     *                      {@code klass}
     * @return a {@link ServletBuilder} instance allowing for further configuration
     */
    public ServletBuilder addServlet(Class<? extends Servlet> klass,
                                           String urlPattern) {
        final ServletHolder holder = new ServletHolder(checkNotNull(klass));
        final ServletBuilder servletConfig = new ServletBuilder(holder, servlets);
        servletConfig.addUrlPattern(checkNotNull(urlPattern));
        return servletConfig;
    }

    /**
     * Add a filter instance.
     *
     * @param filter        the filter instance
     * @param urlPattern    the URL pattern for requests that should be handled by {@code filter}
     * @return a {@link FilterBuilder} instance allowing for further configuration
     */
    public FilterBuilder addFilter(Filter filter,
                                         String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(filter));
        final FilterBuilder filterConfig = new FilterBuilder(holder, filters);
        filterConfig.addUrlPattern(checkNotNull(urlPattern));
        return filterConfig;
    }

    /**
     * Add a filter class.
     *
     * @param klass         the filter class
     * @param urlPattern    the URL pattern for requests that should be handled by instances of
     *                      {@code klass}
     * @return a {@link FilterBuilder} instance allowing for further configuration
     */
    public FilterBuilder addFilter(Class<? extends Filter> klass,
                                         String urlPattern) {
        final FilterHolder holder = new FilterHolder(checkNotNull(klass));
        final FilterBuilder filterConfig = new FilterBuilder(holder, filters);
        filterConfig.addUrlPattern(checkNotNull(urlPattern));
        return filterConfig;
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
     * Adds a protected Target (ie a target that 404s)
     *
     * @param target  a protected target
     */
    public void addProtectedTarget(String target) {
        protectedTargets.add(checkNotNull(target));
    }

    public void setBaseResource(Resource baseResource) {
        this.baseResource = baseResource;
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
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
    public void setJerseyProperty(String name, @Nullable Object value) {
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
        manage(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS, nameFormat));
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
        manage(new ExecutorServiceManager(executor, 5, TimeUnit.SECONDS, nameFormat));
        return executor;
    }

    /*
     * Internal Accessors
     */

    ImmutableSet<HealthCheck> getHealthChecks() {
        return healthChecks.build();
    }

    ImmutableMap<String, ServletHolder> getServlets() {
        addServlet(jerseyServletContainer,
                   configuration.getHttpConfiguration()
                                .getRootPath()).setInitOrder(Integer.MAX_VALUE);
        return servlets.build();
    }

    ImmutableMultimap<String, FilterHolder> getFilters() {
        return filters.build();
    }

    ImmutableSet<Task> getTasks() {
        return tasks.build();
    }

    ImmutableSet<String> getProtectedTargets() {
        return protectedTargets.build();
    }

    Resource getBaseResource() {
        return baseResource;
    }

    ImmutableSet<EventListener> getServletListeners() {
        return servletListeners.build();
    }

    private void logManagedObjects() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (Object bean : lifeCycle.getBeans()) {
            builder.add(bean.toString());
        }
        LOGGER.debug("managed objects = {}", builder.build());
    }

    private void logHealthChecks() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (HealthCheck healthCheck : healthChecks.build()) {
            final String canonicalName = healthCheck.getClass().getCanonicalName();
            if (canonicalName == null) {
                builder.add(String.format("%s(\"%s\")", HealthCheck.class.getCanonicalName(), healthCheck.getName()));
            } else {
                builder.add(canonicalName);
            }
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
                String rootPath = configuration.getHttpConfiguration().getRootPath();
                if (rootPath.endsWith("/*")) {
                    rootPath = rootPath.substring(0, rootPath.length()-2);
                }
                final StringBuilder pathBuilder = new StringBuilder()
                        .append(rootPath)
                        .append(path);
                if (method.isAnnotationPresent(Path.class)) {
                    final String methodPath = method.getAnnotation(Path.class).value();
                    if (!methodPath.startsWith("/") && !path.endsWith("/")) {
                        pathBuilder.append('/');
                    }
                    pathBuilder.append(methodPath);
                }
                for (HttpMethod verb : method.getMetaMethodAnnotations(HttpMethod.class)) {
                    endpoints.add(String.format("    %-7s %s (%s)",
                                                verb.value(),
                                                pathBuilder.toString(),
                                                klass.getCanonicalName()));
                }
            }

            for (String line : Ordering.natural().sortedCopy(endpoints.build())) {
                stringBuilder.append(line).append('\n');
            }
        }

        LOGGER.info(stringBuilder.toString());
    }

    private void logTasks() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append("\n\n");

        for (Task task : tasks.build()) {
            stringBuilder.append(String.format("    %-7s /tasks/%s (%s)\n",
                                               "POST", task.getName(), task.getClass().getCanonicalName()));
        }

        LOGGER.info("tasks = {}", stringBuilder.toString());
    }

    private MethodList annotatedMethods(Class<?> resource) {
        return new MethodList(resource, true).hasMetaAnnotation(HttpMethod.class);
    }

    public SessionHandler getSessionHandler() {
        return sessionHandler;
    }

    public ObjectMapperFactory getObjectMapperFactory() {
        return objectMapperFactory;
    }

    public ResourceConfig getJerseyResourceConfig() {
        return config;
    }

    public ServletContainer getJerseyServletContainer() {
        return jerseyServletContainer;
    }

    public void setJerseyServletContainer(ServletContainer jerseyServletContainer) {
        this.jerseyServletContainer = checkNotNull(jerseyServletContainer);
    }

    public String getName() {
        return name;
    }
}
