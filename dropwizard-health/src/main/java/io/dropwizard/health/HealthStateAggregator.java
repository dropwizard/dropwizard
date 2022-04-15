package io.dropwizard.health;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface HealthStateAggregator {
    @NotNull
    Collection<HealthStateView> healthStateViews();

    @NotNull
    Optional<HealthStateView> healthStateView(@NotNull String name);
}
