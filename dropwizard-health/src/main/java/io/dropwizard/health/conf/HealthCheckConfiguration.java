package io.dropwizard.health.conf;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class HealthCheckConfiguration {

    @NotNull
    @Size(min = 1)
    @JsonProperty
    private String name = "";

    @NotNull
    @JsonProperty
    private HealthCheckType type = HealthCheckType.READY;

    @JsonProperty
    private boolean critical = false;

    @JsonProperty
    private boolean initialState = true;

    @Valid
    @NotNull
    @JsonProperty
    private Schedule schedule = new Schedule();

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public HealthCheckType getType() {
        return type;
    }

    public void setType(HealthCheckType type) {
        this.type = type;
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(final boolean critical) {
        this.critical = critical;
    }

    public boolean isInitialState() {
        return initialState;
    }

    public void setInitialState(boolean initialState) {
        this.initialState = initialState;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(final Schedule schedule) {
        this.schedule = schedule;
    }
}
