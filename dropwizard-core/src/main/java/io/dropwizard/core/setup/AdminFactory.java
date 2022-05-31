package io.dropwizard.core.setup;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.servlets.tasks.TaskConfiguration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.StringJoiner;

/**
 * A factory for configuring the admin interface for the environment.
 *
 * @since 2.0
 */
public class AdminFactory {
    @Valid
    @NotNull
    private HealthCheckConfiguration healthChecks = new HealthCheckConfiguration();

    @Valid
    @NotNull
    private TaskConfiguration tasks = new TaskConfiguration();

    @JsonProperty("healthChecks")
    public HealthCheckConfiguration getHealthChecks() {
        return healthChecks;
    }

    @JsonProperty("healthChecks")
    public void setHealthChecks(HealthCheckConfiguration healthChecks) {
        this.healthChecks = healthChecks;
    }

    @JsonProperty("tasks")
    public TaskConfiguration getTasks() {
        return tasks;
    }

    @JsonProperty("tasks")
    public void setTasks(TaskConfiguration tasks) {
        this.tasks = tasks;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", AdminFactory.class.getSimpleName() + "[", "]")
                .add("healthChecks=" + healthChecks)
                .add("tasks=" + tasks)
                .toString();
    }
}
