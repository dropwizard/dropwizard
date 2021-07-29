package io.dropwizard.health;

import java.util.Collection;
import java.util.Optional;

public interface HealthStateAggregator {
    Collection<HealthStateView> healthStateViews();

    Optional<HealthStateView> healthStateView(String name);
}
