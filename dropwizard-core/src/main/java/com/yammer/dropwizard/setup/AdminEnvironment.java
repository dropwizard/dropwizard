package com.yammer.dropwizard.setup;

import com.google.common.collect.ImmutableSet;
import com.yammer.dropwizard.tasks.GarbageCollectionTask;
import com.yammer.dropwizard.tasks.Task;
import com.yammer.dropwizard.tasks.TaskServlet;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class AdminEnvironment extends ServletEnvironment {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminEnvironment.class);

    private final ServletContextHandler handler;
    private final Set<HealthCheck> healthChecks;
    private final TaskServlet tasks;

    public AdminEnvironment(ServletContextHandler handler,
                            Set<HealthCheck> healthChecks) {
        super(handler);
        this.handler = handler;
        this.healthChecks = healthChecks;
        this.tasks = new TaskServlet();
        tasks.add(new GarbageCollectionTask());
        handler.addServlet(new ServletHolder(tasks), "/tasks/*");
        handler.addLifeCycleListener(new AbstractLifeCycle.AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle event) {
                logTasks();
                logHealthChecks();
            }
        });
    }

    public void addTask(Task task) {
        tasks.add(checkNotNull(task));
    }

    /**
     * Adds the given health check to the set of health checks exposed on the admin port.
     *
     * @param healthCheck a health check
     */
    public void addHealthCheck(HealthCheck healthCheck) {
        healthChecks.add(checkNotNull(healthCheck));
    }

    private void logTasks() {
        final StringBuilder stringBuilder = new StringBuilder(1024).append("\n\n");

        for (Task task : tasks.getTasks()) {
            stringBuilder.append(String.format("    %-7s /tasks/%s (%s)\n",
                                               "POST",
                                               task.getName(),
                                               task.getClass().getCanonicalName()));
        }

        LOGGER.info("tasks = {}", stringBuilder.toString());
    }

    private void logHealthChecks() {
        if (healthChecks.isEmpty()) {
            LOGGER.warn('\n' +
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                                "!    THIS SERVICE HAS NO HEALTHCHECKS. THIS MEANS YOU WILL NEVER KNOW IF IT    !\n" +
                                "!    DIES IN PRODUCTION, WHICH MEANS YOU WILL NEVER KNOW IF YOU'RE LETTING     !\n" +
                                "!     YOUR USERS DOWN. YOU SHOULD ADD A HEALTHCHECK FOR EACH DEPENDENCY OF     !\n" +
                                "!     YOUR SERVICE WHICH FULLY (BUT LIGHTLY) TESTS YOUR SERVICE'S ABILITY TO   !\n" +
                                "!      USE THAT SERVICE. THINK OF IT AS A CONTINUOUS INTEGRATION TEST.         !\n" +
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n" +
                                "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!"
            );
        }

        final ImmutableSet.Builder<String> builder = ImmutableSet.builder();
        for (HealthCheck healthCheck : healthChecks) {
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
}
