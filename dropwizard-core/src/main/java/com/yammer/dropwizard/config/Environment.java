package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jetty.JettyManaged;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.lifecycle.ExecutorServiceManager;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.dropwizard.lifecycle.ServerLifecycleListener;
import com.yammer.dropwizard.setup.JerseyEnvironment;
import com.yammer.dropwizard.setup.ServletEnvironment;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.AggregateLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

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
    private final DropwizardResourceConfig config;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ServletContextHandler servletContext;
    private final ImmutableSet.Builder<Task> tasks;
    private final ImmutableList.Builder<ServerLifecycleListener> serverListeners;
    private final AggregateLifeCycle lifeCycle;
    private final ObjectMapperFactory objectMapperFactory;
    private SessionHandler sessionHandler;
    private Validator validator;

    private final AtomicReference<ServletContainer> jerseyServletContainer;
    private final ServletEnvironment servletEnvironment;
    private final JerseyEnvironment jerseyEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name                the name of the service
     * @param objectMapperFactory the {@link ObjectMapperFactory} for the service
     */
    public Environment(String name,
                       ObjectMapperFactory objectMapperFactory,
                       Validator validator) {
        this.name = name;
        this.objectMapperFactory = objectMapperFactory;
        this.validator = validator;
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
        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);
        this.tasks = ImmutableSet.builder();
        this.serverListeners = ImmutableList.builder();
        this.lifeCycle = new AggregateLifeCycle();
        this.jerseyServletContainer = new AtomicReference<ServletContainer>(new ServletContainer(config));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, config);
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

    public JerseyEnvironment getJerseyEnvironment() {
        return jerseyEnvironment;
    }

    /**
     * Adds the given health check to the set of health checks exposed on the admin port.
     *
     * @param healthCheck a health check
     */
    public void addHealthCheck(HealthCheck healthCheck) {
        healthChecks.add(checkNotNull(healthCheck));
    }

    /**
     * Adds the given {@link Managed} instance to the set of objects managed by the server's
     * lifecycle. When the server starts, {@code managed} will be started. When the server stops,
     * {@code managed} will be stopped.
     *
     * @param managed a managed object
     */
    public void manage(Managed managed) {
        lifeCycle.addBean(new JettyManaged(checkNotNull(managed)));
    }

    /**
     * Adds the given Jetty {@link LifeCycle} instances to the server's lifecycle.
     *
     * @param managed a Jetty-managed object
     */
    public void manage(LifeCycle managed) {
        lifeCycle.addBean(checkNotNull(managed));
    }

    /**
     * Returns the servlet environment.
     *
     * @return the servlet environment
     */
    public ServletEnvironment getServletEnvironment() {
        return servletEnvironment;
    }

    /**
     * Adds a {@link Task} instance.
     *
     * @param task a {@link Task}
     */
    public void addTask(Task task) {
        tasks.add(checkNotNull(task));
    }

    public void setSessionHandler(SessionHandler sessionHandler) {
        this.sessionHandler = sessionHandler;
    }


    /**
     * Creates a new {@link ExecutorService} instance with the given parameters whose lifecycle is
     * managed by the service.
     *
     * @param nameFormat      a {@link String#format(String, Object...)}-compatible format String,
     *                        to which a unique integer (0, 1, etc.) will be supplied as the single
     *                        parameter.
     * @param corePoolSize    the number of threads to keep in the pool, even if they are idle.
     * @param maximumPoolSize the maximum number of threads to allow in the pool.
     * @param keepAliveTime   when the number of threads is greater than the core, this is the
     *                        maximum time that excess idle threads will wait for new tasks before
     *                        terminating.
     * @param unit            the time unit for the keepAliveTime argument.
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
     * @param nameFormat   a {@link String#format(String, Object...)}-compatible format String, to
     *                     which a unique integer (0, 1, etc.) will be supplied as the single
     *                     parameter.
     * @param corePoolSize the number of threads to keep in the pool, even if they are idle.
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

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = checkNotNull(validator);
    }

    /*
    * Internal Accessors
    */

    ImmutableSet<HealthCheck> getHealthChecks() {
        return healthChecks.build();
    }

    ServletContextHandler getServletContextHandler() {
        return servletContext;
    }

    ImmutableSet<Task> getTasks() {
        return tasks.build();
    }

    ServletContainer getJerseyServletContainer() {
        return jerseyServletContainer.get();
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
                builder.add(String.format("%s(\"%s\")",
                                          HealthCheck.class.getCanonicalName(),
                                          healthCheck.getName()));
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
        final StringBuilder stringBuilder = new StringBuilder(1024).append("The following paths were found for the configured resources:\n\n");

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
            String rootPath = jerseyEnvironment.getUrlPattern();
            if (rootPath.endsWith("/*")) {
                rootPath = rootPath.substring(0, rootPath.length() - (path.startsWith("/") ? 2 : 1));
            }

            final ImmutableList.Builder<String> endpoints = ImmutableList.builder();
            for (AnnotatedMethod method : annotatedMethods(klass)) {
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
                                               "POST",
                                               task.getName(),
                                               task.getClass().getCanonicalName()));
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

    public String getName() {
        return name;
    }

    public List<ServerLifecycleListener> getServerListeners() {
        return serverListeners.build();
    }

    public void addServerLifecycleListener(ServerLifecycleListener listener) {
        serverListeners.add(listener);
    }
}
