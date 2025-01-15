package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistryListener;
import io.dropwizard.util.Duration;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

class HealthCheckManager implements HealthCheckRegistryListener, HealthStatusChecker, ShutdownNotifier,
    HealthStateListener, HealthStateAggregator {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckManager.class);

    private final AtomicBoolean isAppAlive = new AtomicBoolean(true);
    private final AtomicBoolean isAppHealthy = new AtomicBoolean(false);
    private final AtomicInteger unhealthyCriticalHealthChecks = new AtomicInteger();
    private final AtomicInteger unhealthyCriticalAliveChecks = new AtomicInteger();
    @NonNull
    private final HealthCheckScheduler scheduler;
    @NonNull
    private final Map<String, HealthCheckConfiguration> configs;
    @NonNull
    private final Collection<HealthStateListener> healthStateListeners;
    @NonNull
    private final MetricRegistry metrics;
    private final Duration shutdownWaitPeriod;
    private final boolean initialOverallState;
    @NonNull
    private final String aggregateHealthyName;
    @NonNull
    private final String aggregateUnhealthyName;
    @NonNull
    private Map<String, ScheduledHealthCheck> checks;
    private volatile boolean shuttingDown = false;

    public HealthCheckManager(final List<HealthCheckConfiguration> configs,
                              final HealthCheckScheduler scheduler,
                              final MetricRegistry metrics,
                              final Duration shutdownWaitPeriod,
                              final boolean initialOverallState,
                              final Collection<HealthStateListener> healthStateListeners) {
        this.configs = configs.stream()
            .collect(Collectors.toMap(HealthCheckConfiguration::getName, Function.identity()));
        this.scheduler = Objects.requireNonNull(scheduler);
        this.metrics = Objects.requireNonNull(metrics);
        this.shutdownWaitPeriod = shutdownWaitPeriod;
        this.initialOverallState = initialOverallState;
        this.checks = new HashMap<>();
        this.healthStateListeners = Objects.requireNonNull(healthStateListeners);

        this.aggregateHealthyName = MetricRegistry.name("health", "aggregate", "healthy");
        this.aggregateUnhealthyName = MetricRegistry.name("health", "aggregate", "unhealthy");
        metrics.register(aggregateHealthyName, (Gauge<Long>) this::calculateNumberOfHealthyChecks);
        metrics.register(aggregateUnhealthyName, (Gauge<Long>) this::calculateNumberOfUnhealthyChecks);
    }

    // visible for testing
    void setChecks(final Map<String, ScheduledHealthCheck> checks) {
        this.checks = checks;
    }

    @Override
    public void onHealthCheckAdded(final String name, final HealthCheck healthCheck) {
        final HealthCheckConfiguration config = configs.get(name);

        if (config == null) {
            LOGGER.debug("ignoring registered health check that isn't configured: name={}", name);
            return;
        }

        final Schedule schedule = config.getSchedule();
        final HealthCheckType type = config.getType();
        // type of 'alive' implies 'critical'
        final boolean critical = (type == HealthCheckType.ALIVE) || config.isCritical();
        final boolean initialState = config.isInitialState();

        final State state = new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), initialState, this);
        final Counter healthyCheckCounter = metrics.counter(MetricRegistry.name("health", name, "healthy"));
        final Counter unhealthyCheckCounter = metrics.counter(MetricRegistry.name("health", name, "unhealthy"));

        final ScheduledHealthCheck check = new ScheduledHealthCheck(name, type, critical, healthCheck, schedule, state,
            healthyCheckCounter, unhealthyCheckCounter);
        checks.put(name, check);

        // handle initial state of 'false' to ensure counts line up
        if (!initialState && critical) {
            handleCriticalHealthChange(check, false);
        }

        scheduler.scheduleInitial(check);
    }

    @Override
    public void onHealthCheckRemoved(final String name, final HealthCheck healthCheck) {
        scheduler.unschedule(name);
    }

    @Override
    public void onStateChanged(final String name, final boolean isNowHealthy) {
        LOGGER.debug("health check changed state: name={} state={}", name, isNowHealthy);
        final ScheduledHealthCheck check = checks.get(name);

        if (check == null) {
            LOGGER.error("State changed for unconfigured health check: name={} state={}", name, isNowHealthy);
            return;
        }

        if (check.isCritical()) {
            handleCriticalHealthChange(check, isNowHealthy);
        } else {
            handleNonCriticalHealthChange(check, isNowHealthy);
        }

        scheduler.schedule(check, isNowHealthy);

        healthStateListeners.forEach(listener -> {
            try {
                listener.onStateChanged(name, isNowHealthy);
            } catch (final RuntimeException e) {
                LOGGER.warn("Exception thrown for healthCheckName: {} from Health State listener onStateChanged: {}",
                    name, listener, e);
                // swallow error
            }
        });
        healthStateListeners.forEach(listener -> listener.onStateChanged(name, isNowHealthy));
    }

    protected void initializeAppHealth() {
        this.isAppHealthy.set(initialOverallState);
    }

    private long calculateNumberOfHealthyChecks() {
        return checks.values()
            .stream()
            .filter(ScheduledHealthCheck::isHealthy)
            .count();
    }

    private long calculateNumberOfUnhealthyChecks() {
        return checks.values()
            .stream()
            .filter(check -> !check.isHealthy())
            .count();
    }

    private void handleCriticalHealthChange(final ScheduledHealthCheck healthCheck, final boolean isNowHealthy) {
        if (isNowHealthy) {
            LOGGER.info("A critical dependency is now healthy: name={}, type={}",
                healthCheck.getName(), healthCheck.getType());
            switch (healthCheck.getType()) {
                case ALIVE:
                    updateCriticalStatus(isAppAlive, unhealthyCriticalAliveChecks.decrementAndGet());
                    return;
                case READY:
                    if (!shuttingDown) {
                        updateCriticalStatus(isAppHealthy, unhealthyCriticalHealthChecks.decrementAndGet());
                    } else {
                        LOGGER.info("Status change is ignored during shutdown: name={}, type={}",
                            healthCheck.getName(), healthCheck.getType());
                    }
                    return;
            }
        } else {
            HealthCheckConfiguration healthCheckConfiguration = configs.get(healthCheck.getName());
            if (healthCheckConfiguration != null
                && !healthCheckConfiguration.isInitialState() && !healthCheck.isPreviouslyRecovered()) {
                LOGGER.warn("A critical unhealthy initialized dependency has not yet recovered: name={}, type={}",
                    healthCheck.getName(), healthCheck.getType());
            } else {
                LOGGER.error("A critical dependency is now unhealthy: name={}, type={}",
                    healthCheck.getName(), healthCheck.getType());
            }
            switch (healthCheck.getType()) {
                case ALIVE:
                    updateCriticalStatus(isAppAlive, unhealthyCriticalAliveChecks.incrementAndGet());
                    return;
                case READY:
                    updateCriticalStatus(isAppHealthy, unhealthyCriticalHealthChecks.incrementAndGet());
                    return;
            }
        }
        LOGGER.warn("Unexpected health check type: type={}", healthCheck.getType());
    }

    private void updateCriticalStatus(final AtomicBoolean status, final int count) {
        status.set(count == 0);
        LOGGER.debug("current status: unhealthy-critical={}", count);
    }

    private void handleNonCriticalHealthChange(final ScheduledHealthCheck healthCheck, final boolean isNowHealthy) {
        if (isNowHealthy) {
            LOGGER.info("A non-critical dependency is now healthy: name={}, type={}",
                healthCheck.getName(), healthCheck.getType());

            if (!isHealthy() && checks.values().stream().filter(ScheduledHealthCheck::isCritical).allMatch(ScheduledHealthCheck::isHealthy)) {
                isAppHealthy.compareAndSet(false, true);
            }
        } else {
            HealthCheckConfiguration healthCheckConfiguration = configs.get(healthCheck.getName());
            if (healthCheckConfiguration != null
                && !healthCheckConfiguration.isInitialState() && !healthCheck.isPreviouslyRecovered()) {
                LOGGER.info("A non-critical unhealthy initialized dependency has not yet recovered: name={}, type={}",
                    healthCheck.getName(), healthCheck.getType());
            } else {
                LOGGER.warn("A non-critical dependency is now unhealthy: name={}, type={}",
                    healthCheck.getName(), healthCheck.getType());
            }
        }
    }

    String getAggregateHealthyName() {
        return aggregateHealthyName;
    }

    String getAggregateUnhealthyName() {
        return aggregateUnhealthyName;
    }

    @Override
    public boolean isHealthy() {
        return isAppAlive.get() && isAppHealthy.get();
    }

    @Override
    public boolean isHealthy(@Nullable String type) {
        if (HealthCheckType.ALIVE.name().equalsIgnoreCase(type)) {
            return isAppAlive.get();
        } else {
            return isHealthy();
        }
    }

    @Override
    public void notifyShutdownStarted() throws Exception {
        shuttingDown = true;
        LOGGER.info("delayed shutdown: started (waiting {})", shutdownWaitPeriod);

        // set healthy to false to indicate to the load balancer that it should not be in rotation for requests
        isAppHealthy.set(false);

        // sleep for period of time to give time for load balancer to realize requests should not be sent anymore
        Thread.sleep(shutdownWaitPeriod.toMilliseconds());

        LOGGER.info("delayed shutdown: finished");
    }

    @Override
    public void onHealthyCheck(final String healthCheckName) {
        healthStateListeners.forEach(listener -> {
            try {
                listener.onHealthyCheck(healthCheckName);
            } catch (final RuntimeException e) {
                LOGGER.warn("Exception thrown for healthCheckName: {} from Health State listener onHealthyCheck: {}",
                    healthCheckName, listener, e);
                // swallow error
            }
        });
    }

    @Override
    public void onUnhealthyCheck(final String healthCheckName) {
        healthStateListeners.forEach(listener -> {
            try {
                listener.onUnhealthyCheck(healthCheckName);
            } catch (final RuntimeException e) {
                LOGGER.warn("Exception thrown for healthCheckName: {} from Health State listener onUnhealthyCheck: {}",
                    healthCheckName, listener, e);
                // swallow error
            }
        });
    }

    @NonNull
    @Override
    public Collection<HealthStateView> healthStateViews() {
        return checks.values()
            .stream()
            .map(ScheduledHealthCheck::view)
            .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public Optional<HealthStateView> healthStateView(@NonNull final String name) {
        return Optional.ofNullable(checks.get(name))
            .map(ScheduledHealthCheck::view);
    }
}
