package io.dropwizard.health;

import java.util.Collection;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.NonNull;

public interface HealthStateAggregator {
    @NonNull
    Collection<HealthStateView> healthStateViews();

    @NonNull
    Optional<HealthStateView> healthStateView(@NonNull String name);
}
