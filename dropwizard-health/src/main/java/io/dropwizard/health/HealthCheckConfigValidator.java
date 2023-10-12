package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class HealthCheckConfigValidator implements Managed {
    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckConfigValidator.class);

    private final List<HealthCheckConfiguration> configs;
    private final HealthCheckRegistry registry;

    public HealthCheckConfigValidator(List<HealthCheckConfiguration> configs, HealthCheckRegistry registry) {
        this.configs = configs;
        this.registry = registry;
    }

    @Override
    public void start() throws Exception {
        validateConfiguration(configs, registry.getNames());
    }

    private void validateConfiguration(final List<HealthCheckConfiguration> healthCheckConfigs,
                                       final Set<String> registeredHealthCheckNames) {
        final Set<String> configuredHealthCheckNames = healthCheckConfigs.stream()
            .map(HealthCheckConfiguration::getName)
            .collect(Collectors.toSet());

        // find health checks that are registered but do not have a configured schedule
        final List<String> notConfiguredHealthCheckNames = registeredHealthCheckNames.stream()
            .filter(healthCheckName -> !configuredHealthCheckNames.contains(healthCheckName))
            .sorted()
            .collect(Collectors.toList());

        if (!notConfiguredHealthCheckNames.isEmpty()) {
            final String healthCheckList = notConfiguredHealthCheckNames.stream()
                .map(name -> "  * " + name)
                .collect(Collectors.joining("\n"));
            LOGGER.info("The following health check(s) were registered, but are not configured with a schedule:\n{}",
                healthCheckList);
        }

        // find health checks that are configured with a schedule but are not actually registered
        final List<String> notRegisteredHealthCheckNames = configuredHealthCheckNames.stream()
            .filter(healthCheckName -> !registeredHealthCheckNames.contains(healthCheckName))
            .sorted()
            .collect(Collectors.toList());

        if (!notRegisteredHealthCheckNames.isEmpty()) {
            final String healthCheckList = notRegisteredHealthCheckNames.stream()
                .map(name -> "  * " + name)
                .collect(Collectors.joining("\n"));
            LOGGER.error("The following health check(s) are configured with a schedule, but were not registered:\n{}",
                healthCheckList);
            throw new IllegalStateException("The following configured health checks were not registered: "
                + notRegisteredHealthCheckNames);
        }
    }
}
