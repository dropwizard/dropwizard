package io.dropwizard.health;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistryListener;
import io.dropwizard.health.conf.HealthCheckConfiguration;
import io.dropwizard.health.conf.HealthCheckType;
import io.dropwizard.health.conf.Schedule;
import io.dropwizard.health.shutdown.ShutdownNotifier;
import io.dropwizard.util.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckManager implements HealthCheckRegistryListener, StateChangedCallback, HealthStatusChecker,
        ShutdownNotifier {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckManager.class);

    private final AtomicBoolean isAppAlive = new AtomicBoolean(true);
    private final AtomicBoolean isAppHealthy = new AtomicBoolean(false);
    private final AtomicInteger unhealthyCriticalHealthChecks = new AtomicInteger();
    private final AtomicInteger unhealthyCriticalAliveChecks = new AtomicInteger();
    private final HealthCheckScheduler scheduler;
    private final Map<String, ScheduledHealthCheck> checks;
    private final Map<String, HealthCheckConfiguration> configs;
    private final MetricRegistry metrics;
    private final Duration shutdownWaitPeriod;
    private final boolean initialOverallState;
    private final String aggregateHealthyName;
    private final String aggregateUnhealthyName;
    private volatile boolean shuttingDown = false;

    public HealthCheckManager(final List<HealthCheckConfiguration> configs,
                              final HealthCheckScheduler scheduler,
                              final MetricRegistry metrics,
                              final Duration shutdownWaitPeriod,
                              final boolean initialOverallState) {
        this(configs, scheduler, metrics, shutdownWaitPeriod, initialOverallState, new HashMap<>());
    }

    // Visible for testing
    HealthCheckManager(final List<HealthCheckConfiguration> configs,
                       final HealthCheckScheduler scheduler,
                       final MetricRegistry metrics,
                       final Duration shutdownWaitPeriod,
                       final boolean initialOverallState,
                       final Map<String, ScheduledHealthCheck> checks) {
        this.configs = configs.stream()
                .collect(Collectors.toMap(HealthCheckConfiguration::getName, Function.identity()));
        this.scheduler = Objects.requireNonNull(scheduler);
        this.metrics = Objects.requireNonNull(metrics);
        this.shutdownWaitPeriod = shutdownWaitPeriod;
        this.initialOverallState = initialOverallState;
        this.checks = Objects.requireNonNull(checks);

        this.aggregateHealthyName = MetricRegistry.name("health", "aggregate", "healthy");
        this.aggregateUnhealthyName = MetricRegistry.name("health", "aggregate", "unhealthy");
        metrics.register(aggregateHealthyName, (Gauge) this::calculateNumberOfHealthyChecks);
        metrics.register(aggregateUnhealthyName, (Gauge) this::calculateNumberOfUnhealthyChecks);
    }

    @Override
    public void onHealthCheckAdded(final String name, final HealthCheck healthCheck) {
        final HealthCheckConfiguration config = configs.get(name);

        if (config == null) {
            log.debug("ignoring registered health check that isn't configured: name={}", name);
            return;
        }

        final Schedule schedule = config.getSchedule();
        final HealthCheckType type = config.getType();
        // type of 'alive' implies 'critical'
        final boolean critical = (type == HealthCheckType.ALIVE) || config.isCritical();
        final boolean initialState = config.isInitialState();

        final State state = new State(name, schedule.getFailureAttempts(), schedule.getSuccessAttempts(), initialState, this);
        final Counter healthyCheckCounter = metrics.counter(MetricRegistry.name("health", name, "healthy"));
        final Counter unhealthyCheckCounter = metrics.counter(MetricRegistry.name("health",  name, "unhealthy"));

        final ScheduledHealthCheck check = new ScheduledHealthCheck(name, type, critical, healthCheck, schedule, state,
                healthyCheckCounter, unhealthyCheckCounter);
        checks.put(name, check);

        // handle initial state of 'false' to ensure counts line up
        if (!initialState && critical) {
            handleCriticalHealthChange(name, type, false);
        }

        scheduler.scheduleInitial(check);
    }

    @Override
    public void onHealthCheckRemoved(final String name, final HealthCheck healthCheck) {
        scheduler.unschedule(name);
    }

    @Override
    public void onStateChanged(final String name, final boolean isNowHealthy) {
        log.debug("health check changed state: name={} state={}", name, isNowHealthy);
        final ScheduledHealthCheck check = checks.get(name);

        if (check == null) {
            log.error("State changed for unconfigured health check: name={} state={}", name, isNowHealthy);
            return;
        }

        if (check.isCritical()) {
            handleCriticalHealthChange(check.getName(), check.getType(), isNowHealthy);
        } else {
            handleNonCriticalHealthChange(check.getName(), check.getType(), isNowHealthy);
        }

        scheduler.schedule(check, isNowHealthy);
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

    private void handleCriticalHealthChange(final String name, final HealthCheckType type, final boolean isNowHealthy) {
        if (isNowHealthy) {
            log.info("A critical dependency is now healthy: name={}, type={}", name, type);
            switch (type) {
                case ALIVE:
                    updateCriticalStatus(isAppAlive, unhealthyCriticalAliveChecks.decrementAndGet());
                    return;
                case READY:
                    if (!shuttingDown) {
                        updateCriticalStatus(isAppHealthy, unhealthyCriticalHealthChecks.decrementAndGet());
                    } else {
                        log.info("Status change is ignored during shutdown: name={}, type={}", name, type);
                    }
                    return;
            }
        } else {
            log.error("A critical dependency is now unhealthy: name={}, type={}", name, type);
            switch (type) {
                case ALIVE:
                    updateCriticalStatus(isAppAlive, unhealthyCriticalAliveChecks.incrementAndGet());
                    return;
                case READY:
                    updateCriticalStatus(isAppHealthy, unhealthyCriticalHealthChecks.incrementAndGet());
                    return;
            }
        }
        log.warn("Unexpected health check type: type={}", type);
    }

    private void updateCriticalStatus(final AtomicBoolean status, final int count) {
        status.set(count == 0);
        log.debug("current status: unhealthy-critical={}", count);
    }

    private void handleNonCriticalHealthChange(final String name, final HealthCheckType type, final boolean isNowHealthy) {
        if (isNowHealthy) {
            log.info("A non-critical dependency is now healthy: name={}, type={}", name, type);
        } else {
            log.warn("A non-critical dependency is now unhealthy: name={}, type={}", name, type);
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
        log.info("delayed shutdown: started (waiting {})", shutdownWaitPeriod);

        // set healthy to false to indicate to the load balancer that it should not be in rotation for requests
        isAppHealthy.set(false);

        // sleep for period of time to give time for load balancer to realize requests should not be sent anymore
        Thread.sleep(shutdownWaitPeriod.toMilliseconds());

        log.info("delayed shutdown: finished");
    }
}
