package io.dropwizard.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckSchedulerTest {

    @Mock
    private ScheduledExecutorService executor;

    private HealthCheckScheduler scheduler;

    @BeforeEach
    void setUp() {
        this.scheduler = new HealthCheckScheduler(executor);
    }

    @Test
    void shouldScheduleCheckForNotAlreadyScheduledHealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName()).thenReturn(name);
        when(check.getSchedule()).thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(check, schedule.getCheckInterval().toMilliseconds(),
            schedule.getCheckInterval().toMilliseconds(), TimeUnit.MILLISECONDS))
            .thenReturn(mock(ScheduledFuture.class));

        scheduler.schedule(check, true);

        verify(executor).scheduleWithFixedDelay(check, schedule.getCheckInterval().toMilliseconds(),
            schedule.getCheckInterval().toMilliseconds(), TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldScheduleCheckForNotAlreadyScheduledUnhealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName())
            .thenReturn(name);
        when(check.getSchedule())
            .thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(check, schedule.getDowntimeInterval().toMilliseconds(),
            schedule.getDowntimeInterval().toMilliseconds(), TimeUnit.MILLISECONDS))
            .thenReturn(mock(ScheduledFuture.class));

        scheduler.schedule(check, false);

        verify(executor).scheduleWithFixedDelay(check, schedule.getDowntimeInterval().toMilliseconds(),
            schedule.getDowntimeInterval().toMilliseconds(), TimeUnit.MILLISECONDS);
    }

    @Test
    void shouldRescheduleCheckForHealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();
        final ScheduledFuture future = mock(ScheduledFuture.class);

        when(future.cancel(true)).thenReturn(true);

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName()).thenReturn(name);
        when(check.getSchedule()).thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(
            eq(check),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            eq(TimeUnit.MILLISECONDS))
        )
            .thenReturn(future);

        scheduler.schedule(check, false);

        scheduler.schedule(check, true);

        verify(executor, times(2)).scheduleWithFixedDelay(
            eq(check),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            eq(TimeUnit.MILLISECONDS));

        verify(future).cancel(true);
    }

    @Test
    void shouldRescheduleCheckForUnhealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();
        final ScheduledFuture future = mock(ScheduledFuture.class);

        when(future.cancel(true)).thenReturn(true);

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName()).thenReturn(name);
        when(check.getSchedule()).thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(
            eq(check),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            eq(TimeUnit.MILLISECONDS))
        )
            .thenReturn(future);

        scheduler.schedule(check, true);

        scheduler.schedule(check, false);

        verify(executor, times(2)).scheduleWithFixedDelay(
            eq(check),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            eq(TimeUnit.MILLISECONDS));

        verify(future).cancel(true);
    }

    @Test
    void shouldUnscheduleExistingCheck() {
        final String name = "test";
        final Schedule schedule = new Schedule();
        final ScheduledFuture future = mock(ScheduledFuture.class);

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName()).thenReturn(name);
        when(check.getSchedule()).thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(
            eq(check),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            or(eq(schedule.getCheckInterval().toMilliseconds()), eq(schedule.getDowntimeInterval().toMilliseconds())),
            eq(TimeUnit.MILLISECONDS))
        )
            .thenReturn(future);

        scheduler.schedule(check, true);

        scheduler.unschedule(name);

        verify(executor).scheduleWithFixedDelay(
            check,
            schedule.getCheckInterval().toMilliseconds(),
            schedule.getCheckInterval().toMilliseconds(),
            TimeUnit.MILLISECONDS);

        verify(future).cancel(true);
    }

    @Test
    void unscheduleShouldDoNothingIfNoCheckScheduled() {
        final String name = "test";

        assertThatCode(() -> scheduler.unschedule(name))
            .doesNotThrowAnyException();
    }
}
