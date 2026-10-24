package io.dropwizard.health;

import java.util.Set;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Optional;

public interface HealthStateAggregator {
    @NonNull
    Collection<HealthStateView> healthStateViews();

    @NonNull
    Optional<HealthStateView> healthStateView(@NonNull String name);

    @NonNull
    Set<HealthStateView> healthStateViewByType(@NonNull String type);
}
