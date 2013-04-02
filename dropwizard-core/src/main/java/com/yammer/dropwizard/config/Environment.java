package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.setup.JerseyEnvironment;
import com.yammer.dropwizard.setup.LifecycleEnvironment;
import com.yammer.dropwizard.setup.ServletEnvironment;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dropwizard service's environment.
 */
public class Environment {
    private static final Logger LOGGER = LoggerFactory.getLogger(Environment.class);

    private final String name;
    private final ImmutableSet.Builder<HealthCheck> healthChecks;
    private final ImmutableSet.Builder<Task> tasks;

    private final ObjectMapperFactory objectMapperFactory;
    private Validator validator;

    private final DropwizardResourceConfig jerseyConfig;
    private final AtomicReference<ServletContainer> jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final ServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final Server server;
    private final LifecycleEnvironment lifecycleEnvironment;

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
        this.jerseyConfig = new DropwizardResourceConfig(false) {
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
        this.tasks = ImmutableSet.builder();

        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.server = new Server();
        this.lifecycleEnvironment = new LifecycleEnvironment(server);

        this.jerseyServletContainer = new AtomicReference<ServletContainer>(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);

        addTask(new GarbageCollectionTask());
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

    public LifecycleEnvironment getLifecycleEnvironment() {
        return lifecycleEnvironment;
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


    public ObjectMapperFactory getObjectMapperFactory() {
        return objectMapperFactory;
    }

    public String getName() {
        return name;
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

    Server getServer() {
        return server;
    }

    private void logManagedObjects() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (Object bean : server.getBeans()) {
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

        for (Class<?> klass : jerseyConfig.getClasses()) {
            if (klass.isAnnotationPresent(Path.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : jerseyConfig.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("resources = {}", builder.build());
    }

    private void logProviders() {
        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();

        for (Class<?> klass : jerseyConfig.getClasses()) {
            if (klass.isAnnotationPresent(Provider.class)) {
                builder.add(klass.getCanonicalName());
            }
        }

        for (Object o : jerseyConfig.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Provider.class)) {
                builder.add(o.getClass().getCanonicalName());
            }
        }

        LOGGER.debug("providers = {}", builder.build());
    }

    private void logEndpoints() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append("The following paths were found for the configured resources:\n\n");

        final ImmutableList.Builder<Class<?>> builder = ImmutableList.builder();
        for (Object o : jerseyConfig.getSingletons()) {
            if (o.getClass().isAnnotationPresent(Path.class)) {
                builder.add(o.getClass());
            }
        }
        for (Class<?> klass : jerseyConfig.getClasses()) {
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
}
