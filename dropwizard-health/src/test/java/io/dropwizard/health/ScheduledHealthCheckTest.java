package io.dropwizard.health;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ScheduledHealthCheckTest {
    private static final HealthStateListener LISTENER = new HealthStateListener() {
        @Override
        public void onHealthyCheck(String healthCheckName) {}

        @Override
        public void onUnhealthyCheck(String healthCheckName) {}

        @Override
        public void onStateChanged(String healthCheckName, boolean healthy) {}
    };
    private final MetricRegistry metrics = new MetricRegistry();

    @Mock
    private HealthCheck healthCheck;

    @Mock
    private Schedule schedule;

    @Test
    void healthyCheckShouldResultInSuccess() {
        when(schedule.getSuccessAttempts()).thenReturn(1);
        when(schedule.getFailureAttempts()).thenReturn(1);

        final String name = "test";

        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state =
                new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), false, LISTENER);
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(
                name, HealthCheckType.READY, true, healthCheck, schedule, state, healthyCounter, unhealthyCounter);

        when(healthCheck.execute()).thenReturn(HealthCheck.Result.healthy());

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isFalse();

        scheduledHealthCheck.run();

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isTrue();

        assertThat(scheduledHealthCheck.isHealthy()).isTrue();
        assertThat(healthyCounter.getCount()).isEqualTo(1L);
        assertThat(unhealthyCounter.getCount()).isZero();
    }

    @Test
    void unhealthyCheckShouldResultInFail() {
        when(schedule.getSuccessAttempts()).thenReturn(1);
        when(schedule.getFailureAttempts()).thenReturn(1);

        final String name = "test";
        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state =
                new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), false, LISTENER);
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(
                name, HealthCheckType.READY, true, healthCheck, schedule, state, healthyCounter, unhealthyCounter);
        when(healthCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("something happened"));

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isFalse();

        scheduledHealthCheck.run();

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isFalse();

        assertThat(scheduledHealthCheck.isHealthy()).isFalse();
        assertThat(healthyCounter.getCount()).isZero();
        assertThat(unhealthyCounter.getCount()).isEqualTo(1L);
    }
}
