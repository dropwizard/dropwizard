package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.util.Duration;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class Schedule {

    @Nullable
    @JsonProperty
    private Duration initialDelay = null;

    @NotNull
    @JsonProperty
    private Duration checkInterval = Duration.seconds(5);

    @NotNull
    @JsonProperty
    private Duration downtimeInterval = Duration.seconds(30);

    @Min(0)
    @JsonProperty
    private int failureAttempts = 3;

    @Min(0)
    @JsonProperty
    private int successAttempts = 2;

    public Duration getInitialDelay() {
        // default to checkInterval value
        return initialDelay == null ? getCheckInterval() : initialDelay;
    }

    public void setInitialDelay(Duration initialDelay) {
        this.initialDelay = initialDelay;
    }

    public Duration getCheckInterval() {
        return checkInterval;
    }

    public void setCheckInterval(final Duration checkInterval) {
        this.checkInterval = checkInterval;
    }

    public Duration getDowntimeInterval() {
        return downtimeInterval;
    }

    public void setDowntimeInterval(final Duration downtimeInterval) {
        this.downtimeInterval = downtimeInterval;
    }

    public int getFailureAttempts() {
        return failureAttempts;
    }

    public void setFailureAttempts(final int failureAttempts) {
        this.failureAttempts = failureAttempts;
    }

    public int getSuccessAttempts() {
        return successAttempts;
    }

    public void setSuccessAttempts(final int successAttempts) {
        this.successAttempts = successAttempts;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Schedule schedule)) return false;
        return failureAttempts == schedule.failureAttempts &&
            successAttempts == schedule.successAttempts &&
            Objects.equals(initialDelay, schedule.initialDelay) &&
            Objects.equals(checkInterval, schedule.checkInterval) &&
            Objects.equals(downtimeInterval, schedule.downtimeInterval);
    }

    @Override
    public int hashCode() {
        return Objects.hash(initialDelay, checkInterval, downtimeInterval, failureAttempts, successAttempts);
    }
}
