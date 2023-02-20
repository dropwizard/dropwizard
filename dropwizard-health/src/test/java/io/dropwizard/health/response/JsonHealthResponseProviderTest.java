package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthCheckType;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStateView;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.Jackson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JsonHealthResponseProviderTest {
    private final ObjectMapper mapper = Jackson.newObjectMapper();
    @Mock
    private HealthStatusChecker healthStatusChecker;
    @Mock
    private HealthStateAggregator healthStateAggregator;
    private JsonHealthResponseProvider jsonHealthResponseProvider;

    @BeforeEach
    void setUp() {
        this.jsonHealthResponseProvider = new JsonHealthResponseProvider(healthStatusChecker,
            healthStateAggregator, mapper);
    }

    @Test
    void shouldHandleSingleHealthStateViewCorrectly() throws IOException {
        // given
        final HealthStateView view = new HealthStateView("foo", true, HealthCheckType.READY, true);
        final Map<String, Collection<String>> queryParams = Collections.singletonMap(
            JsonHealthResponseProvider.NAME_QUERY_PARAM, Collections.singleton(view.getName()));

        // when
        when(healthStateAggregator.healthStateView(view.getName())).thenReturn(Optional.of(view));
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = jsonHealthResponseProvider.healthResponse(queryParams);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(mapper.readTree(response.getMessage())).isEqualTo(mapper.readTree(fixture("/json/single-healthy-response.json")));
    }

    @Test
    void shouldHandleMultipleHealthStateViewsCorrectly() throws IOException {
        // given
        final HealthStateView fooView = new HealthStateView("foo", true, HealthCheckType.READY, true);
        final HealthStateView barView = new HealthStateView("bar", true, HealthCheckType.ALIVE, true);
        final HealthStateView bazView = new HealthStateView("baz", false, HealthCheckType.READY, false);
        final Collection<String> names = new ArrayList<>();
        names.add(fooView.getName());
        names.add(barView.getName());
        names.add(bazView.getName());
        final Map<String, Collection<String>> queryParams = Collections.singletonMap(
            JsonHealthResponseProvider.NAME_QUERY_PARAM, names);

        // when
        when(healthStateAggregator.healthStateView(fooView.getName())).thenReturn(Optional.of(fooView));
        when(healthStateAggregator.healthStateView(barView.getName())).thenReturn(Optional.of(barView));
        when(healthStateAggregator.healthStateView(bazView.getName())).thenReturn(Optional.of(bazView));
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = jsonHealthResponseProvider.healthResponse(queryParams);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(mapper.readTree(response.getMessage())).isEqualTo(mapper.readTree(fixture("/json/multiple-healthy-responses.json")));
    }

    @Test
    void shouldHandleZeroHealthStateViewsCorrectly() {
        // given
        // when
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = jsonHealthResponseProvider.healthResponse(Collections.emptyMap());

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getMessage()).isEqualToIgnoringWhitespace("[]");
        verifyNoInteractions(healthStateAggregator);
    }

    @Test
    void shouldThrowExceptionWhenJsonProcessorExceptionOccurs() throws IOException {
        // given
        final ObjectMapper mapperMock = mock(ObjectMapper.class);
        this.jsonHealthResponseProvider = new JsonHealthResponseProvider(healthStatusChecker,
            healthStateAggregator, mapperMock);
        final HealthStateView view = new HealthStateView("foo", true, HealthCheckType.READY, true);
        final Map<String, Collection<String>> queryParams = Collections.singletonMap(
            JsonHealthResponseProvider.NAME_QUERY_PARAM, Collections.singleton(view.getName()));
        final JsonMappingException exception = JsonMappingException.fromUnexpectedIOE(new IOException("uh oh"));

        // when
        when(healthStateAggregator.healthStateView(view.getName())).thenReturn(Optional.of(view));
        when(mapperMock.writeValueAsString(any()))
            .thenThrow(exception);

        // then
        assertThatThrownBy(() -> jsonHealthResponseProvider.healthResponse(queryParams))
            .isInstanceOf(RuntimeException.class)
            .hasCauseReference(exception);
        verifyNoInteractions(healthStatusChecker);
    }

    private String fixture(final String filename) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(filename)) {
            return new String(requireNonNull(is).readAllBytes(), UTF_8);
        }
    }
}
