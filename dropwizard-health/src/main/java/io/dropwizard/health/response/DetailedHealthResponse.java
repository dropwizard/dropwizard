package io.dropwizard.health.response;

import io.dropwizard.health.HealthStateView;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public final class DetailedHealthResponse extends HealthResponse {
    @Nonnull
    private final Collection<HealthStateView> views;

    DetailedHealthResponse(boolean healthy, final @Nonnull String message, final @Nonnull String contentType,
                           final Collection<HealthStateView> views) {
        super(healthy, Objects.requireNonNull(message), contentType);
        this.views = Objects.requireNonNull(views);
    }

    @Nonnull
    @Override
    public Optional<String> getMessage() {
        return Optional.of(Objects.requireNonNull(this.message));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetailedHealthResponse)) return false;
        if (!super.equals(o)) return false;
        DetailedHealthResponse that = (DetailedHealthResponse) o;
        return views.equals(that.views);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), views);
    }
}
