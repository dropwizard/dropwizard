package io.dropwizard.request.logging.filter;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

class UriFilterFactoryTest {
    private final IAccessEvent accessEvent = mock(IAccessEvent.class);
    private final UriFilterFactory filterFactory = new UriFilterFactory();

    @BeforeEach
    void setUp() {
        reset(accessEvent);
    }

    @Test
    void shouldDenyLogsForConfiguredUri() {
        final String path = "/health-check";
        filterFactory.setUris(Collections.singleton(path));
        final Filter filter = filterFactory.build();

        when(accessEvent.getRequestURI()).thenReturn(path);

        assertThat(filter.decide(accessEvent))
            .isEqualTo(FilterReply.DENY);
    }

    @Test
    void shouldNotDenyUnconfiguredUriLogs() {
        filterFactory.setUris(Collections.emptySet());
        final Filter filter = filterFactory.build();

        when(accessEvent.getRequestURI()).thenReturn("/foo");

        assertThat(filter.decide(accessEvent))
            .isEqualTo(FilterReply.NEUTRAL);
    }

    @Test
    void shouldDenyLogsForAdditionalConfiguredUris() {
        final Set<String> paths = new HashSet<>();
        paths.add("/health-check");
        paths.add("/sys/health");
        filterFactory.setUris(paths);

        final Filter filter = filterFactory.build();

        when(accessEvent.getRequestURI()).thenReturn("/sys/health");

        assertThat(filter.decide(accessEvent))
            .isEqualTo(FilterReply.DENY);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()).contains(UriFilterFactory.class);
    }
}
