package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Collections.unmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckManagerTest {
    private static final String NAME = "test";
    private static final String NAME_2 = "test2";
    private static final HealthCheckType READY = HealthCheckType.READY;
    private static final Duration SHUTDOWN_WAIT = Duration.seconds(5);

    @Mock
    private HealthCheckScheduler scheduler;

    @Test
    void shouldIgnoreUnconfiguredAddedHealthChecks() {
        // given
        final HealthCheckManager manager = new HealthCheckManager(Collections.emptyList(), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());

        // when
        manager.onHealthCheckAdded(NAME, mock(HealthCheck.class));

        // then
        verifyNoInteractions(scheduler);
        assertThat(manager.healthStateView(NAME))
            .isEmpty();
        assertThat(manager.healthStateViews())
            .isEmpty();
    }

    @Test
    void shouldScheduleHealthCheckWhenConfiguredHealthCheckAdded() {
        // given
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setCritical(true);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());

        // when
        manager.onHealthCheckAdded(NAME, mock(HealthCheck.class));

        // then
        verifyCheckWasScheduled(scheduler, true);
        assertThat(manager.healthStateViews())
            .singleElement()
            .isEqualTo(manager.healthStateView(NAME).orElseThrow(IllegalStateException::new))
            .satisfies(view -> assertThat(view.getName()).isEqualTo(NAME));
    }

    @Test
    void shouldUnscheduleTaskWhenHealthCheckRemoved() {
        // given
        final ScheduledHealthCheck healthCheck = mock(ScheduledHealthCheck.class);
        final HealthCheckManager manager = new HealthCheckManager(Collections.emptyList(), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());
        manager.setChecks(singletonMap(NAME, healthCheck));

        // when
        manager.onHealthCheckRemoved(NAME, mock(HealthCheck.class));

        // then
        verify(scheduler).unschedule(NAME);
        assertThat(manager.healthStateView(NAME))
            .isEmpty();
        assertThat(manager.healthStateViews())
            .singleElement()
            .isNull();
    }

    @Test
    void shouldDoNothingWhenStateChangesForUnconfiguredHealthCheck() {
        // given
        final HealthCheckManager manager = new HealthCheckManager(Collections.emptyList(), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());

        // when
        manager.onStateChanged(NAME, false);

        // then
        verifyNoInteractions(scheduler);
    }

    @Test
    void shouldReportUnhealthyWhenInitialOverallStateIsFalse() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setCritical(true);
        config.setInitialState(false);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, false, Collections.emptyList());
        manager.initializeAppHealth();
        final HealthCheck check = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, check);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isFalse())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        manager.onStateChanged(NAME, true);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isTrue())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        verifyCheckWasScheduled(scheduler, true);
    }

    @Test
    void shouldReportHealthyWhenInitialOverallStateIsFalseAndReadyCheckIsHealthy() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setType(HealthCheckType.READY);
        config.setCritical(false);
        config.setInitialState(false);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, false, Collections.emptyList());
        manager.initializeAppHealth();
        final HealthCheck check = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, check);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isFalse())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        manager.onStateChanged(NAME, true);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isTrue())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue())
            .satisfies(m -> assertThat(m.isHealthy("ready")).isTrue());

        verifyCheckWasScheduled(scheduler, false);
    }

    @Test
    void shouldMarkServerUnhealthyWhenCriticalHealthCheckFails() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setCritical(true);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());
        manager.initializeAppHealth();
        final HealthCheck check = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, check);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isTrue())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        manager.onStateChanged(NAME, false);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isFalse())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        verifyCheckWasScheduled(scheduler, true);
    }

    @Test
    void shouldMarkServerNotAliveAndUnhealthyWhenCriticalAliveCheckFails() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setType(HealthCheckType.ALIVE);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());
        manager.initializeAppHealth();
        final HealthCheck check = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, check);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isTrue())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isTrue());

        manager.onStateChanged(NAME, false);
        assertThat(manager)
            .satisfies(m -> assertThat(m.isHealthy()).isFalse())
            .satisfies(m -> assertThat(m.isHealthy("alive")).isFalse());

        verifyCheckWasScheduled(scheduler, true);
    }

    @Test
    void shouldMarkServerHealthyWhenCriticalHealthCheckRecovers() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setCritical(true);
        config.setSchedule(new Schedule());
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());
        final HealthCheck check = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, check);
        manager.onStateChanged(NAME, false);
        assertThat(manager.isHealthy()).isFalse();

        manager.onStateChanged(NAME, true);
        assertThat(manager.isHealthy()).isTrue();

        ArgumentCaptor<ScheduledHealthCheck> checkCaptor = ArgumentCaptor.forClass(ScheduledHealthCheck.class);
        ArgumentCaptor<Boolean> healthyCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(scheduler).scheduleInitial(checkCaptor.capture());
        verify(scheduler, times(2)).schedule(checkCaptor.capture(), healthyCaptor.capture());

        assertThat(checkCaptor.getAllValues())
            .hasSize(3)
            .allMatch(value -> NAME.equals(value.getName()))
            .allMatch(ScheduledHealthCheck::isCritical);

        assertThat(healthyCaptor.getAllValues())
            .containsExactly(false, true);
    }

    @Test
    void shouldNotChangeServerStateWhenNonCriticalHealthCheckFails() {
        final HealthCheckConfiguration config = new HealthCheckConfiguration();
        config.setName(NAME);
        config.setCritical(false);
        config.setSchedule(new Schedule());
        final HealthCheck check = mock(HealthCheck.class);
        final HealthCheckManager manager = new HealthCheckManager(singletonList(config), scheduler,
            new MetricRegistry(), SHUTDOWN_WAIT, true, Collections.emptyList());
        manager.initializeAppHealth();

        manager.onHealthCheckAdded(NAME, check);
        manager.onStateChanged(NAME, false);
        assertThat(manager.isHealthy()).isTrue();

        verifyCheckWasScheduled(scheduler, false);
    }

    @Test
    void shouldNotChangeServerStateWhenNonCriticalHealthCheckRecovers() {
        final List<HealthCheckConfiguration> configs = new ArrayList<>();
        final HealthCheckConfiguration nonCriticalConfig = new HealthCheckConfiguration();
        nonCriticalConfig.setName(NAME);
        nonCriticalConfig.setCritical(false);
        nonCriticalConfig.setSchedule(new Schedule());
        nonCriticalConfig.setInitialState(false);
        configs.add(nonCriticalConfig);
        final HealthCheckConfiguration criticalConfig = new HealthCheckConfiguration();
        criticalConfig.setName(NAME_2);
        criticalConfig.setCritical(true);
        criticalConfig.setSchedule(new Schedule());
        criticalConfig.setInitialState(false);
        configs.add(criticalConfig);
        final HealthCheckManager manager = new HealthCheckManager(unmodifiableList(configs), scheduler, new MetricRegistry(),
            SHUTDOWN_WAIT, true, Collections.emptyList());
        final HealthCheck nonCriticalCheck = mock(HealthCheck.class);
        final HealthCheck criticalCheck = mock(HealthCheck.class);

        manager.onHealthCheckAdded(NAME, nonCriticalCheck);
        manager.onHealthCheckAdded(NAME_2, criticalCheck);

        manager.onStateChanged(NAME, false);
        manager.onStateChanged(NAME_2, false);
        assertThat(manager.isHealthy()).isFalse();

        manager.onStateChanged(NAME, true);
        assertThat(manager.isHealthy()).isFalse();

        ArgumentCaptor<ScheduledHealthCheck> checkCaptor = ArgumentCaptor.forClass(ScheduledHealthCheck.class);
        ArgumentCaptor<Boolean> healthyCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(scheduler, times(2)).scheduleInitial(checkCaptor.capture());
        verify(scheduler, times(3)).schedule(checkCaptor.capture(), healthyCaptor.capture());

        assertThat(checkCaptor.getAllValues())
            .hasSize(5)
            .satisfies(values -> assertThat(values).element(0)
                .satisfies(value -> assertThat(value.getName()).isEqualTo(NAME))
                .satisfies(value -> assertThat(value.isCritical()).isFalse()))
            .satisfies(values -> assertThat(values).element(1)
                .satisfies(value -> assertThat(value.getName()).isEqualTo(NAME_2))
                .satisfies(value -> assertThat(value.isCritical()).isTrue()))
            .satisfies(values -> assertThat(values).element(2)
                .satisfies(value -> assertThat(value.getName()).isEqualTo(NAME))
                .satisfies(value -> assertThat(value.isCritical()).isFalse()))
            .satisfies(values -> assertThat(values).element(3)
                .satisfies(value -> assertThat(value.getName()).isEqualTo(NAME_2))
                .satisfies(value -> assertThat(value.isCritical()).isTrue()))
            .satisfies(values -> assertThat(values).element(4)
                .satisfies(value -> assertThat(value.getName()).isEqualTo(NAME))
                .satisfies(value -> assertThat(value.isCritical()).isFalse()));
        assertThat(healthyCaptor.getAllValues()).containsExactly(false, false, true);
    }

    @Test
    void shouldRecordNumberOfHealthyAndUnhealthyHealthChecks() {
        // given
        final Schedule schedule = new Schedule();
        final List<HealthCheckConfiguration> configs = new ArrayList<>();
        final HealthCheckConfiguration nonCriticalConfig = new HealthCheckConfiguration();
        nonCriticalConfig.setName(NAME);
        nonCriticalConfig.setCritical(false);
        nonCriticalConfig.setSchedule(schedule);
        configs.add(nonCriticalConfig);
        final HealthCheckConfiguration criticalConfig = new HealthCheckConfiguration();
        criticalConfig.setName(NAME_2);
        criticalConfig.setCritical(true);
        criticalConfig.setSchedule(schedule);
        configs.add(criticalConfig);
        final HealthCheck check = mock(HealthCheck.class);
        final MetricRegistry metrics = new MetricRegistry();
        final AtomicInteger healthyCounter = new AtomicInteger();
        final AtomicInteger unhealthyCounter = new AtomicInteger();

        final HealthStateListener countingListener = new HealthStateListener() {
            @Override
            public void onHealthyCheck(String healthCheckName) {
                healthyCounter.incrementAndGet();
            }

            @Override
            public void onUnhealthyCheck(String healthCheckName) {
                unhealthyCounter.incrementAndGet();
            }

            @Override
            public void onStateChanged(String healthCheckName, boolean healthy) {
            }
        };
        final HealthCheckManager manager = new HealthCheckManager(unmodifiableList(configs), scheduler, metrics, SHUTDOWN_WAIT, true,
            Collections.singleton(countingListener));

        final ScheduledHealthCheck check1 = new ScheduledHealthCheck(NAME, READY, nonCriticalConfig.isCritical(), check,
            schedule, new State(NAME, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), true, manager),
            metrics.counter(NAME + ".healthy"), metrics.counter(NAME + ".unhealthy"));
        final ScheduledHealthCheck check2 = new ScheduledHealthCheck(NAME_2, READY, criticalConfig.isCritical(), check,
            schedule, new State(NAME, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), true, manager),
            metrics.counter(NAME_2 + ".healthy"), metrics.counter(NAME_2 + ".unhealthy"));
        manager.setChecks(Map.of(NAME, check1, NAME_2, check2));

        // then
        assertThat(metrics.gauge(manager.getAggregateHealthyName(), null).getValue())
            .isEqualTo(2L);
        assertThat(metrics.gauge(manager.getAggregateUnhealthyName(), null).getValue())
            .isEqualTo(0L);

        // when
        when(check.execute()).thenReturn(HealthCheck.Result.unhealthy("because"));
        // Fail 3 times, to trigger unhealthy state change
        check2.run();
        check2.run();
        check2.run();

        // then
        assertThat(metrics.gauge(manager.getAggregateHealthyName(), null).getValue())
            .isEqualTo(1L);
        assertThat(metrics.gauge(manager.getAggregateUnhealthyName(), null).getValue())
            .isEqualTo(1L);
        assertThat(unhealthyCounter).hasValue(3);
        assertThat(healthyCounter).hasValue(0);
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    void shouldContinueScheduledCheckingWhileDelayingShutdown() throws Exception {
        // given
        final int checkIntervalMillis = 10;
        final int shutdownWaitTimeMillis = 50;
        final int expectedCount = shutdownWaitTimeMillis / checkIntervalMillis - 1;
        final AtomicBoolean shutdownFailure = new AtomicBoolean(false);
        final CountingHealthCheck check = new CountingHealthCheck();
        final Schedule schedule = new Schedule();
        schedule.setCheckInterval(Duration.milliseconds(checkIntervalMillis));
        final HealthCheckConfiguration checkConfig = new HealthCheckConfiguration();
        checkConfig.setName("check1");
        checkConfig.setCritical(true);
        checkConfig.setSchedule(schedule);
        final List<HealthCheckConfiguration> configs = singletonList(checkConfig);
        final ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1);
        final HealthCheckScheduler scheduler = new HealthCheckScheduler(executorService);
        final MetricRegistry metrics = new MetricRegistry();
        final Duration shutdownWaitPeriod = Duration.milliseconds(shutdownWaitTimeMillis);

        // when
        final HealthCheckManager manager = new HealthCheckManager(configs, scheduler, metrics, shutdownWaitPeriod,
            true, Collections.emptyList());
        manager.onHealthCheckAdded("check1", check);
        // simulate JVM shutdown hook
        final Thread shutdownThread = new Thread(() -> {
            try {
                manager.notifyShutdownStarted();
            } catch (Exception e) {
                shutdownFailure.set(true);
                e.printStackTrace();
            }
        });
        Thread.sleep(20);
        long beforeCount = check.getCount();
        shutdownThread.start();
        shutdownThread.join();
        Thread.sleep(20);
        long afterCount = check.getCount();

        // then
        assertThat(shutdownFailure).isFalse();
        assertThat(afterCount - beforeCount).isGreaterThanOrEqualTo(expectedCount);
    }

    private void verifyCheckWasScheduled(HealthCheckScheduler scheduler, boolean critical) {
        ArgumentCaptor<ScheduledHealthCheck> checkCaptor = ArgumentCaptor.forClass(ScheduledHealthCheck.class);
        verify(scheduler).scheduleInitial(checkCaptor.capture());
        assertThat(checkCaptor.getValue())
            .satisfies(value -> assertThat(value.getName()).isEqualTo(HealthCheckManagerTest.NAME))
            .satisfies(value -> assertThat(value.isCritical()).isEqualTo(critical));
    }

    private static class CountingHealthCheck extends HealthCheck {
        private static final Logger log = LoggerFactory.getLogger(CountingHealthCheck.class);
        private final Counter counter = new Counter();

        public long getCount() {
            return counter.getCount();
        }

        @Override
        protected Result check() {
            counter.inc();
            log.info("count={}", getCount());
            return Result.healthy();
        }
    }
}
