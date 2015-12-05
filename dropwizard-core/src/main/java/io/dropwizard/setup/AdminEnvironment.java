package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.servlets.tasks.GarbageCollectionTask;
import io.dropwizard.servlets.tasks.LogConfigurationTask;
import io.dropwizard.servlets.tasks.Task;
import io.dropwizard.servlets.tasks.TaskServlet;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.Objects.requireNonNull;

/**
 * The administrative environment of a Dropwizard application.
 */
public class AdminEnvironment extends ServletEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminEnvironment.class);

    private final HealthCheckRegistry healthChecks;
    private final TaskServlet tasks;

    /**
     * Creates a new {@link AdminEnvironment}.
     *
     * @param handler      a servlet context handler
     * @param healthChecks a health check registry
     */
    public AdminEnvironment(MutableServletContextHandler handler,
                            HealthCheckRegistry healthChecks, MetricRegistry metricRegistry) {
        super(handler);
        this.healthChecks = healthChecks;
        this.healthChecks.register("deadlocks", new ThreadDeadlockHealthCheck());
        this.tasks = new TaskServlet(metricRegistry);
        tasks.add(new GarbageCollectionTask());
        tasks.add(new LogConfigurationTask());
        addServlet("tasks", tasks).addMapping("/tasks/*");
        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                logTasks();
                logHealthChecks();
            }
        });
    }

    /**
     * Adds the given task to the set of tasks exposed via the admin interface.
     *
     * @param task a task
     */
    public void addTask(Task task) {
        tasks.add(requireNonNull(task));
    }

    private void logTasks() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append(String.format("%n%n"));

        for (Task task : tasks.getTasks()) {
            final String taskClassName = firstNonNull(task.getClass().getCanonicalName(), task.getClass().getName());
            stringBuilder.append(String.format("    %-7s /tasks/%s (%s)%n",
                                               "POST",
                                               task.getName(),
                                               taskClassName));
        }

        LOGGER.info("tasks = {}", stringBuilder.toString());
    }

    private void logHealthChecks() {
        if (healthChecks.getNames().size() <= 1) {
            LOGGER.warn(String.format(
                    "%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!    THIS APPLICATION HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW      !%n" +
                            "!     IF IT DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE      !%n" +
                            "!    LETTING YOUR USERS DOWN. YOU SHOULD ADD A HEALTHCHECK FOR EACH OF YOUR    !%n" +
                            "!         APPLICATION'S DEPENDENCIES WHICH FULLY (BUT LIGHTLY) TESTS IT.       !%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!%n" +
                            "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            ));
        }
        LOGGER.debug("health checks = {}", healthChecks.getNames());
    }
}
