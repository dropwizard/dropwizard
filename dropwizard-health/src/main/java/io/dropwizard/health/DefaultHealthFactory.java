package io.dropwizard.health;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.health.response.HealthResponderFactory;
import io.dropwizard.health.response.HealthResponseProvider;
import io.dropwizard.health.response.HealthResponseProviderFactory;
import io.dropwizard.health.response.JsonHealthResponseProviderFactory;
import io.dropwizard.health.response.ServletHealthResponderFactory;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonList;

@JsonTypeName("default")
public class DefaultHealthFactory implements HealthFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHealthFactory.class);

    private static final String DEFAULT_BASE_NAME = "health-check";
    private static final String DEFAULT_PATH = "/health-check";

    @JsonProperty
    private boolean enabled = true;

    @Valid
    @NotNull
    @JsonProperty
    private List<HealthCheckConfiguration> healthChecks = Collections.emptyList();

    @JsonProperty
    private boolean initialOverallState = true;

    @JsonProperty
    private boolean delayedShutdownHandlerEnabled = false;

    @NotNull
    @JsonProperty
    private Duration shutdownWaitPeriod = Duration.seconds(15);

    @NotNull
    @Size(min = 1)
    @JsonProperty
    private List<String> healthCheckUrlPaths = singletonList(DEFAULT_PATH);

    @Valid
    @JsonProperty("responseProvider")
    private HealthResponseProviderFactory healthResponseProviderFactory =
            new JsonHealthResponseProviderFactory();

    @Valid
    @JsonProperty("responder")
    private HealthResponderFactory healthResponderFactory = new ServletHealthResponderFactory();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @JsonProperty("healthChecks")
    public List<HealthCheckConfiguration> getHealthCheckConfigurations() {
        return healthChecks;
    }

    @JsonProperty("healthChecks")
    public void setHealthCheckConfigurations(final List<HealthCheckConfiguration> healthChecks) {
        this.healthChecks = healthChecks;
    }

    public boolean isInitialOverallState() {
        return initialOverallState;
    }

    public void setInitialOverallState(boolean initialOverallState) {
        this.initialOverallState = initialOverallState;
    }

    public boolean isDelayedShutdownHandlerEnabled() {
        return delayedShutdownHandlerEnabled;
    }

    public void setDelayedShutdownHandlerEnabled(final boolean delayedShutdownHandlerEnabled) {
        this.delayedShutdownHandlerEnabled = delayedShutdownHandlerEnabled;
    }

    public Duration getShutdownWaitPeriod() {
        return shutdownWaitPeriod;
    }

    public void setShutdownWaitPeriod(final Duration shutdownWaitPeriod) {
        this.shutdownWaitPeriod = shutdownWaitPeriod;
    }

    public List<String> getHealthCheckUrlPaths() {
        return healthCheckUrlPaths;
    }

    public void setHealthCheckUrlPaths(final List<String> healthCheckUrlPaths) {
        this.healthCheckUrlPaths = healthCheckUrlPaths;
    }

    public HealthResponseProviderFactory getHealthResponseProviderFactory() {
        return healthResponseProviderFactory;
    }

    public void setHealthResponseProviderFactory(HealthResponseProviderFactory healthResponseProviderFactory) {
        this.healthResponseProviderFactory = healthResponseProviderFactory;
    }

    public HealthResponderFactory getHealthResponderFactory() {
        return healthResponderFactory;
    }

    public void setHealthResponderFactory(HealthResponderFactory healthResponderFactory) {
        this.healthResponderFactory = healthResponderFactory;
    }

    @Override
    public void configure(final LifecycleEnvironment lifecycle, final ServletEnvironment servlets,
                          final JerseyEnvironment jersey, final HealthEnvironment health, final ObjectMapper mapper,
                          final String name) {
        if (!isEnabled()) {
            LOGGER.info("Health check configuration is disabled.");
            return;
        }

        final MetricRegistry metrics = lifecycle.getMetricRegistry();
        final HealthCheckRegistry envHealthChecks = health.healthChecks();

        final String fullName = DEFAULT_BASE_NAME + "-" + name;
        final List<HealthCheckConfiguration> healthCheckConfigs = getHealthCheckConfigurations();

        // setup schedules for configured health checks
        final ScheduledExecutorService scheduledHealthCheckExecutor = createScheduledExecutorForHealthChecks(
                healthCheckConfigs.size(), metrics, lifecycle, fullName);
        final HealthCheckScheduler scheduler = new HealthCheckScheduler(scheduledHealthCheckExecutor);
        // configure health manager to receive registered health state listeners from HealthEnvironment (via reference)
        final HealthCheckManager healthCheckManager = new HealthCheckManager(healthCheckConfigs, scheduler, metrics,
                shutdownWaitPeriod, initialOverallState, health.healthStateListeners());
        healthCheckManager.initializeAppHealth();

        // setup response provider and responder to respond to health check requests
        final HealthResponseProvider responseProvider = healthResponseProviderFactory.build(healthCheckManager,
                healthCheckManager, mapper);
        healthResponderFactory.configure(fullName, healthCheckUrlPaths, responseProvider, health, jersey, servlets,
                mapper);

        // register listener for HealthCheckRegistry and setup validator to ensure correct config
        envHealthChecks.addListener(healthCheckManager);
        lifecycle.manage(new HealthCheckConfigValidator(healthCheckConfigs, envHealthChecks));

        // register shutdown handler with Jetty
        final Duration shutdownDelay = getShutdownWaitPeriod();
        if (isDelayedShutdownHandlerEnabled() && shutdownDelay.toMilliseconds() > 0) {
            final DelayedShutdownHandler shutdownHandler = new DelayedShutdownHandler(healthCheckManager);
            shutdownHandler.register();
            LOGGER.debug("Set up delayed shutdown with delay: {}", shutdownDelay);
        }

        // Set the health state aggregator on the HealthEnvironment
        health.setHealthStateAggregator(healthCheckManager);

        LOGGER.debug("Configured ongoing health check monitoring for healthChecks: {}", getHealthCheckConfigurations());
    }

    private ScheduledExecutorService createScheduledExecutorForHealthChecks(
            final int numberOfScheduledHealthChecks,
            final MetricRegistry metrics,
            final LifecycleEnvironment lifecycle,
            final String fullName) {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                .setNameFormat(fullName + "-%d")
                .setDaemon(true)
                .setUncaughtExceptionHandler((t, e) -> LOGGER.error("Thread={} died due to uncaught exception", t, e))
                .build();

        final InstrumentedThreadFactory instrumentedThreadFactory =
                new InstrumentedThreadFactory(threadFactory, metrics);

        final ScheduledExecutorService scheduledExecutorService =
                lifecycle.scheduledExecutorService(fullName + "-scheduled-executor", instrumentedThreadFactory)
                        .threads(numberOfScheduledHealthChecks)
                        .build();

        return new InstrumentedScheduledExecutorService(scheduledExecutorService, metrics);
    }
}
