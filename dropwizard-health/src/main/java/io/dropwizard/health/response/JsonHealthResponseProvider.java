package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStateView;
import io.dropwizard.health.HealthStatusChecker;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonHealthResponseProvider implements HealthResponseProvider {
    public static final String CHECK_TYPE_QUERY_PARAM = "type";
    public static final String NAME_QUERY_PARAM = "name";
    public static final String ALL_VALUE = "all";
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonHealthResponseProvider.class);
    private static final String MEDIA_TYPE = MediaType.APPLICATION_JSON;

    @NonNull
    private final HealthStatusChecker healthStatusChecker;
    @NonNull
    private final HealthStateAggregator healthStateAggregator;
    @NonNull
    private final ObjectMapper mapper;

    public JsonHealthResponseProvider(@NonNull final HealthStatusChecker healthStatusChecker,
                                      @NonNull final HealthStateAggregator healthStateAggregator,
                                      @NonNull final ObjectMapper mapper) {
        this.healthStatusChecker = Objects.requireNonNull(healthStatusChecker);
        this.healthStateAggregator = Objects.requireNonNull(healthStateAggregator);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @NonNull
    @Override
    public HealthResponse healthResponse(final Map<String, Collection<String>> queryParams) {
        final String type = queryParams.getOrDefault(CHECK_TYPE_QUERY_PARAM, Collections.emptyList())
            .stream()
            .findFirst()
            .orElse(null);

        final Collection<HealthStateView> views = getViews(queryParams);

        final String responseBody;
        try {
            responseBody = mapper.writeValueAsString(views);
        } catch (final Exception e) {
            LOGGER.error("Failed to serialize health state views: {}", views, e);
            throw new RuntimeException(e);
        }
        final boolean healthy = healthStatusChecker.isHealthy(type);

        final int status;
        if (healthy) {
            // HTTP OK
            status = 200;
        } else {
            // HTTP Service unavailable
            status = 503;
        }

        return new HealthResponse(healthy, responseBody, MEDIA_TYPE, status);
    }

    private Set<String> getNamesFromQueryParams(final Map<String, Collection<String>> queryParams) {
        return queryParams.getOrDefault(NAME_QUERY_PARAM, Collections.emptyList())
            .stream()
            // normalize all names to lowercase
            .map(String::toLowerCase)
            // maintain order by using a linked hash set
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Collection<HealthStateView> getViews(final Map<String, Collection<String>> queryParams) {
        final Set<String> names = getNamesFromQueryParams(queryParams);

        if (shouldReturnAllViews(names)) {
            return List.copyOf(healthStateAggregator.healthStateViews());
        } else {
            return names.stream()
                .map(healthStateAggregator::healthStateView)
                .flatMap(Optional::stream)
                .collect(Collectors.toUnmodifiableList());
        }
    }

    private boolean shouldReturnAllViews(final Set<String> names) {
        return names.contains(ALL_VALUE);
    }
}
