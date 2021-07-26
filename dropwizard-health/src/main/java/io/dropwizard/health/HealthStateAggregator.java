package io.dropwizard.health;

import java.util.Collection;

public interface HealthStateAggregator {
    Collection<HealthStateView> healthStateViews();
}
