package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStateView;
import io.dropwizard.health.HealthStatusChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Objects;

@JsonTypeName("detailed-json")
public class DetailedJsonHealthResponseProvider implements HealthResponseProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(DetailedJsonHealthResponseProvider.class);

    @Nonnull
    private final HealthStatusChecker healthStatusChecker;
    @Nonnull
    private final HealthStateAggregator healthStateAggregator;
    @Nonnull
    private final ObjectMapper mapper;

    public DetailedJsonHealthResponseProvider(@Nonnull final HealthStatusChecker healthStatusChecker,
                                              @Nonnull final HealthStateAggregator healthStateAggregator,
                                              @Nonnull final ObjectMapper mapper) {
        this.healthStatusChecker = Objects.requireNonNull(healthStatusChecker);
        this.healthStateAggregator = Objects.requireNonNull(healthStateAggregator);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Nonnull
    @Override
    public HealthResponse currentHealthResponse(@Nullable final String type) {
        final Collection<HealthStateView> healthStateViews = healthStateAggregator.healthStateViews();
        final String responseBody;
        try {
             responseBody = mapper.writeValueAsString(healthStateViews);
        } catch (final Exception e) {
            LOGGER.error("Failed to serialize health state views: {}", healthStateViews, e);
            throw new RuntimeException(e);
        }
        final boolean healthy = healthStatusChecker.isHealthy(type);

        return new HealthResponse(healthy, responseBody, MediaType.APPLICATION_JSON);
    }

    @Nonnull
    @Override
    public String contentType() {
        return MediaType.APPLICATION_JSON;
    }
}
