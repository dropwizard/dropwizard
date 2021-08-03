package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import io.dropwizard.health.HealthCheckType;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStateView;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Resources;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailedJsonHealthResponseProviderTest {
    private final ObjectMapper mapper = Jackson.newObjectMapper();
    @Mock
    private HealthStatusChecker healthStatusChecker;
    @Mock
    private HealthStateAggregator healthStateAggregator;
    private DetailedJsonHealthResponseProvider detailedJsonHealthResponseProvider;

    @BeforeEach
    void setUp() {
        this.detailedJsonHealthResponseProvider = new DetailedJsonHealthResponseProvider(healthStatusChecker,
            healthStateAggregator, mapper);
    }

    @Test
    void fullHealthResponseShouldHandleSingleHealthStateViewCorrectly() {
        // given
        final HealthStateView healthStateView = new HealthStateView("foo", true, HealthCheckType.READY, true);
        final Collection<HealthStateView> views = Collections.singleton(healthStateView);
        final String type = "ready";

        // when
        when(healthStateAggregator.healthStateViews()).thenReturn(views);
        when(healthStatusChecker.isHealthy(eq(type))).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.fullHealthResponse(type);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace(fixture("json/single-healthy-response.json"))
        );
    }

    @Test
    void fullHealthResponseShouldHandleMultipleHealthStateViewsCorrectly() {
        // given
        final Collection<HealthStateView> views = ImmutableList.of(
            new HealthStateView("foo", true, HealthCheckType.READY, true),
            new HealthStateView("bar", true, HealthCheckType.ALIVE, true),
            new HealthStateView("baz", false, HealthCheckType.READY, false));

        // when
        when(healthStateAggregator.healthStateViews()).thenReturn(views);
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.fullHealthResponse(null);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace(fixture("json/multiple-healthy-responses.json"))
        );
    }

    @Test
    void fullHealthResponseShouldHandleZeroHealthStateViewCorrectly() {
        // given
        final Collection<HealthStateView> views = Collections.emptyList();

        // when
        when(healthStateAggregator.healthStateViews()).thenReturn(views);
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.fullHealthResponse(null);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace("[]")
        );
    }

    @Test
    void fullHealthResponseShouldThrowExceptionWhenJsonProcessorExceptionOccurs() throws IOException {
        // given
        final ObjectMapper mapperMock = mock(ObjectMapper.class);
        this.detailedJsonHealthResponseProvider = new DetailedJsonHealthResponseProvider(healthStatusChecker,
            healthStateAggregator, mapperMock);
        final Collection<HealthStateView> views = Collections.singleton(
            new HealthStateView("foo", true, HealthCheckType.READY, true));
        final JsonMappingException exception = JsonMappingException.fromUnexpectedIOE(new IOException("uh oh"));

        // when
        when(healthStateAggregator.healthStateViews()).thenReturn(views);
        when(mapperMock.writeValueAsString(any()))
            .thenThrow(exception);

        // then
        assertThatThrownBy(() -> detailedJsonHealthResponseProvider.fullHealthResponse(null))
            .isInstanceOf(RuntimeException.class)
            .hasCauseReference(exception);
        verifyNoInteractions(healthStatusChecker);
    }

    @Test
    void minimalHealthResponseShouldHandleHealthyResultCorrectly() {
        // given
        // when
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.minimalHealthResponse(null);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getMessage()).isNotPresent();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void minimalHealthResponseShouldHandleUnhealthyResultCorrectly() {
        // given
        // when
        when(healthStatusChecker.isHealthy(eq("alive"))).thenReturn(false);
        final HealthResponse response = detailedJsonHealthResponseProvider.minimalHealthResponse("alive");

        // then
        assertThat(response.isHealthy()).isFalse();
        assertThat(response.getMessage()).isNotPresent();
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void partialHealthResponseShouldHandleSingleHealthStateViewCorrectly() {
        // given
        final HealthStateView healthStateView = new HealthStateView("foo", true, HealthCheckType.READY, true);
        final String type = "ready";
        final String name = healthStateView.getName();

        // when
        when(healthStateAggregator.healthStateView(name))
            .thenReturn(Optional.of(healthStateView));
        when(healthStatusChecker.isHealthy(eq(type))).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.partialHealthResponse(type,
            Collections.singleton(name));

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace(fixture("json/single-healthy-response.json"))
        );
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void partialHealthResponseShouldHandleMultipleHealthStateViewsCorrectly() {
        // given
        final Collection<HealthStateView> views = ImmutableList.of(
            new HealthStateView("foo", true, HealthCheckType.READY, true),
            new HealthStateView("bar", true, HealthCheckType.ALIVE, true),
            new HealthStateView("baz", false, HealthCheckType.READY, false));
        final Collection<String> names = views.stream().map(HealthStateView::getName).collect(Collectors.toList());

        // when
        views.forEach(view -> when(healthStateAggregator.healthStateView(view.getName()))
            .thenReturn(Optional.of(view)));
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true);
        final HealthResponse response = detailedJsonHealthResponseProvider.partialHealthResponse(null, names);

        // then
        assertThat(response.isHealthy()).isTrue();
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace(fixture("json/multiple-healthy-responses.json"))
        );
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void partialHealthResponseShouldHandleZeroHealthStateViewCorrectly() {
        // given
        // when
        final String name = "foo";
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(false);
        when(healthStateAggregator.healthStateView(name)).thenReturn(Optional.empty());
        final HealthResponse response = detailedJsonHealthResponseProvider.partialHealthResponse(null,
            Collections.singleton(name));

        // then
        assertThat(response.isHealthy()).isFalse();
        assertThat(response.getMessage()).hasValueSatisfying(message ->
            assertThat(message).isEqualToIgnoringWhitespace("[]")
        );
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_JSON);
    }

    @Test
    void partialHealthResponseShouldThrowExceptionWhenJsonProcessorExceptionOccurs() throws IOException {
        // given
        final ObjectMapper mapperMock = mock(ObjectMapper.class);
        this.detailedJsonHealthResponseProvider = new DetailedJsonHealthResponseProvider(healthStatusChecker,
            healthStateAggregator, mapperMock);
        final HealthStateView view =
            new HealthStateView("foo", true, HealthCheckType.READY, true);
        final JsonMappingException exception = JsonMappingException.fromUnexpectedIOE(new IOException("uh oh"));

        // when
        when(healthStateAggregator.healthStateView(eq(view.getName()))).thenReturn(Optional.of(view));
        when(mapperMock.writeValueAsString(any()))
            .thenThrow(exception);

        // then
        assertThatThrownBy(() -> detailedJsonHealthResponseProvider.partialHealthResponse(null,
            Collections.singleton(view.getName())))
            .isInstanceOf(RuntimeException.class)
            .hasCauseReference(exception);
        verifyNoInteractions(healthStatusChecker);
    }

    // Duplicated from dropwizard-testing due to circular deps
    private String fixture(final String filename) {
        final URL resource = Resources.getResource(filename);
        try {
            return Resources.toString(resource, Charsets.UTF_8).trim();
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
