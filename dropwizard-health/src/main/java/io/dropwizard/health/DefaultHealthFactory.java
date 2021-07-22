package io.dropwizard.health;

import com.codahale.metrics.InstrumentedScheduledExecutorService;
import com.codahale.metrics.InstrumentedThreadFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.health.conf.HealthCheckConfiguration;
import io.dropwizard.health.response.DefaultHealthServletFactory;
import io.dropwizard.health.response.HealthServletFactory;
import io.dropwizard.health.shutdown.DelayedShutdownHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServlet;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonTypeName("default")
public class DefaultHealthFactory implements HealthFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHealthFactory.class);

    private static final String DEFAULT_BASE_NAME = "health-check";

    @JsonProperty
    private boolean enabled = true;

    @Nullable
    @JsonProperty
    private String name;

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
    private List<String> healthCheckUrlPaths = ImmutableList.of("/health-check");

    @Valid
    @JsonProperty("servlet")
    private HealthServletFactory servletFactory = new DefaultHealthServletFactory();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public List<HealthCheckConfiguration> getHealthCheckConfigurations() {
        return healthChecks;
    }

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

    public HealthServletFactory getServletFactory() {
        return servletFactory;
    }

    public void setServletFactory(HealthServletFactory servletFactory) {
        this.servletFactory = servletFactory;
    }


    public List<HealthCheckConfiguration> getHealthChecks() {
        return healthChecks;
    }

    public void setHealthChecks(List<HealthCheckConfiguration> healthChecks) {
        this.healthChecks = healthChecks;
    }

    @Override
    public void configure(final MetricRegistry metrics, final LifecycleEnvironment lifecycle,
                          final HealthCheckRegistry healthChecks, final ServletEnvironment servlets) {
        if (!isEnabled()) {
            LOGGER.info("Health check configuration is disabled.");
            return;
        }

        final String fullName;
        if (name != null) {
            fullName = DEFAULT_BASE_NAME + "-" + name;
        } else {
            fullName = DEFAULT_BASE_NAME;
        }
        final List<HealthCheckConfiguration> healthCheckConfigs = getHealthCheckConfigurations();

        // setup schedules for configured health checks
        final ScheduledExecutorService scheduledHealthCheckExecutor = createScheduledExecutorForHealthChecks(
                healthCheckConfigs.size(), metrics, lifecycle, fullName);
        final HealthCheckScheduler scheduler = new HealthCheckScheduler(scheduledHealthCheckExecutor);
        final HealthCheckManager healthCheckManager = new HealthCheckManager(healthCheckConfigs, scheduler, metrics,
                shutdownWaitPeriod, initialOverallState);
        healthCheckManager.initializeAppHealth();

        // setup servlet to respond to health check requests
        final HttpServlet servlet = getServletFactory().build(healthCheckManager);
        servlets
                .addServlet(fullName + "-servlet", servlet)
                .addMapping(getHealthCheckUrlPaths().toArray(new String[0]));

        // register listener for HealthCheckRegistry and setup validator to ensure correct config
        healthChecks.addListener(healthCheckManager);
        lifecycle.manage(new HealthCheckConfigValidator(healthCheckConfigs, healthChecks));

        // register shutdown handler with Jetty
        final Duration shutdownWaitPeriod = getShutdownWaitPeriod();
        if (isDelayedShutdownHandlerEnabled() && shutdownWaitPeriod.toMilliseconds() > 0) {
            final DelayedShutdownHandler shutdownHandler = new DelayedShutdownHandler(healthCheckManager);
            shutdownHandler.register();
            LOGGER.debug("Set up delayed shutdown with delay: {}", shutdownWaitPeriod);
        }
        LOGGER.debug("Configured ongoing health check monitoring for healthChecks: {}", getHealthChecks());
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
