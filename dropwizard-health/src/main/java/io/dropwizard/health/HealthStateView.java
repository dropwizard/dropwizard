package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

public final class HealthStateView implements Comparable<HealthStateView> {
    @NotNull
    @JsonProperty
    private String name;

    @JsonProperty
    private boolean healthy;

    @NotNull
    @JsonProperty
    private HealthCheckType type;

    @JsonProperty
    private boolean critical;

    public HealthStateView(@NotNull final String name, boolean healthy, @NotNull final HealthCheckType type,
                           boolean critical) {
        this.name = Objects.requireNonNull(name);
        this.healthy = healthy;
        this.type = Objects.requireNonNull(type);
        this.critical = critical;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHealthy() {
        return healthy;
    }

    public void setHealthy(boolean healthy) {
        this.healthy = healthy;
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

    public void setCritical(boolean critical) {
        this.critical = critical;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthStateView that)) return false;
        return healthy == that.healthy && critical == that.critical && Objects.equals(name, that.name) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, healthy, type, critical);
    }

    @Override
    public String toString() {
        return "HealthStateView{" +
                "name='" + name + '\'' +
                ", healthy=" + healthy +
                ", type=" + type +
                ", critical=" + critical +
                '}';
    }

    @Override
    public int compareTo(final HealthStateView other) {
        return name.compareTo(other.getName());
    }
}
