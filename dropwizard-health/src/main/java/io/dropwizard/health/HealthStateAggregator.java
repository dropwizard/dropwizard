package io.dropwizard.health;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Optional;

public interface HealthStateAggregator {
    @Nonnull
    Collection<HealthStateView> healthStateViews();

    @Nonnull
    Optional<HealthStateView> healthStateView(@Nonnull String name);
}
