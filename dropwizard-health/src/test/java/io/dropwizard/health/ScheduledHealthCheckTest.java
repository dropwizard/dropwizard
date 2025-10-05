package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduledHealthCheckTest {
    private static final HealthStateListener LISTENER = new HealthStateListener() {
        @Override
        public void onHealthyCheck(String healthCheckName) {
        }

        @Override
        public void onUnhealthyCheck(String healthCheckName) {
        }

        @Override
        public void onStateChanged(String healthCheckName, boolean healthy) {
        }
    };
    private final MetricRegistry metrics = new MetricRegistry();
    private final HealthCheck healthCheck = mock();

    @Test
    void healthyCheckShouldResultInSuccess() {
        int successAttempts = 1;
        int failureAttempts = 1;

        final String name = "test";

        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state = new State(name, failureAttempts, successAttempts, false, LISTENER);
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(name, HealthCheckType.READY, true,
            healthCheck, mock(), state, healthyCounter, unhealthyCounter);

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
        int successAttempts = 1;
        int failureAttempts = 1;

        final String name = "test";
        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state = new State(name, failureAttempts, successAttempts, false, LISTENER);
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(name, HealthCheckType.READY, true,
            healthCheck, mock(), state, healthyCounter, unhealthyCounter);
        when(healthCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("something happened"));

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isFalse();

        scheduledHealthCheck.run();

        assertThat(scheduledHealthCheck.isPreviouslyRecovered()).isFalse();

        assertThat(scheduledHealthCheck.isHealthy()).isFalse();
        assertThat(healthyCounter.getCount()).isZero();
        assertThat(unhealthyCounter.getCount()).isEqualTo(1L);
    }
}
