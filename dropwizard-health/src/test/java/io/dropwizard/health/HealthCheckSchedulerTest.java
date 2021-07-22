package io.dropwizard.health;

import io.dropwizard.health.conf.Schedule;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.AdditionalMatchers.or;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class HealthCheckSchedulerTest {

    @Mock
    private ScheduledExecutorService executor;

    private HealthCheckScheduler scheduler;

    @BeforeEach
    public void setUp() {
        this.scheduler = new HealthCheckScheduler(executor);
    }

    @Test
    public void shouldScheduleCheckForNotAlreadyScheduledHealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName()).thenReturn(name);
        when(check.getSchedule()).thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(eq(check), eq(schedule.getCheckInterval().toMilliseconds()),
                eq(schedule.getCheckInterval().toMilliseconds()), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(mock(ScheduledFuture.class));

        scheduler.schedule(check, true);

        verify(executor).scheduleWithFixedDelay(eq(check), eq(schedule.getCheckInterval().toMilliseconds()),
                eq(schedule.getCheckInterval().toMilliseconds()), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldScheduleCheckForNotAlreadyScheduledUnhealthyDependency() {
        final String name = "test";
        final Schedule schedule = new Schedule();

        final ScheduledHealthCheck check = mock(ScheduledHealthCheck.class);
        when(check.getName())
                .thenReturn(name);
        when(check.getSchedule())
                .thenReturn(schedule);

        when(executor.scheduleWithFixedDelay(eq(check), eq(schedule.getDowntimeInterval().toMilliseconds()),
                eq(schedule.getDowntimeInterval().toMilliseconds()), eq(TimeUnit.MILLISECONDS)))
                .thenReturn(mock(ScheduledFuture.class));

        scheduler.schedule(check, false);

        verify(executor).scheduleWithFixedDelay(eq(check), eq(schedule.getDowntimeInterval().toMilliseconds()),
                eq(schedule.getDowntimeInterval().toMilliseconds()), eq(TimeUnit.MILLISECONDS));
    }

    @Test
    public void shouldRescheduleCheckForHealthyDependency() {
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
    public void shouldRescheduleCheckForUnhealthyDependency() {
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
    public void shouldUnscheduleExistingCheck() {
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
                eq(check),
                eq(schedule.getCheckInterval().toMilliseconds()),
                eq(schedule.getCheckInterval().toMilliseconds()),
                eq(TimeUnit.MILLISECONDS));

        verify(future).cancel(true);
    }

    @Test
    public void unscheduleShouldDoNothingIfNoCheckScheduled() {
        final String name = "test";

        scheduler.unschedule(name);
    }
}
