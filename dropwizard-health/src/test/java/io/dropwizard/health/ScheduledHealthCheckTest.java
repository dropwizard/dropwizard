package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.health.conf.HealthCheckType;
import io.dropwizard.health.conf.Schedule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScheduledHealthCheckTest {
    @Mock
    private HealthCheck healthCheck;

    @Mock
    private Schedule schedule;

    private final MetricRegistry metrics = new MetricRegistry();

    @Test
    public void healthyCheckShouldResultInSuccess() {
        when(schedule.getSuccessAttempts()).thenReturn(1);
        when(schedule.getFailureAttempts()).thenReturn(1);

        final String name = "test";

        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state = new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), true,
                (checkName, newState) -> {});
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(name, HealthCheckType.READY, true,
                healthCheck, schedule, state, healthyCounter, unhealthyCounter);

        when(healthCheck.execute()).thenReturn(HealthCheck.Result.healthy());

        scheduledHealthCheck.run();

        assertThat(scheduledHealthCheck.isHealthy()).isTrue();
        assertThat(healthyCounter.getCount()).isEqualTo(1L);
        assertThat(unhealthyCounter.getCount()).isEqualTo(0L);
    }

    @Test
    public void unhealthyCheckShouldResultInFail() {
        when(schedule.getSuccessAttempts()).thenReturn(1);
        when(schedule.getFailureAttempts()).thenReturn(1);

        final String name = "test";
        final Counter healthyCounter = metrics.counter("test.healthy");
        final Counter unhealthyCounter = metrics.counter("test.unhealthy");
        final State state = new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), true,
                (checkName, newState) -> {});
        final ScheduledHealthCheck scheduledHealthCheck = new ScheduledHealthCheck(name, HealthCheckType.READY, true,
                healthCheck, schedule, state, healthyCounter, unhealthyCounter);
        when(healthCheck.execute()).thenReturn(HealthCheck.Result.unhealthy("something happened"));

        scheduledHealthCheck.run();

        assertThat(scheduledHealthCheck.isHealthy()).isFalse();
        assertThat(healthyCounter.getCount()).isEqualTo(0L);
        assertThat(unhealthyCounter.getCount()).isEqualTo(1L);
    }
}
