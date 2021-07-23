package io.dropwizard.health;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.health.conf.HealthCheckConfiguration;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthCheckConfigValidator implements Managed {
    private static final Logger log = LoggerFactory.getLogger(HealthCheckConfigValidator.class);
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

    @Override
    public void stop() throws Exception {
        // do nothing
    }

    private void validateConfiguration(final List<HealthCheckConfiguration> healthCheckConfigs,
                                       final Set<String> registeredHealthCheckNames) {
        final Set<String> configuredHealthCheckNames = healthCheckConfigs.stream()
                .map(HealthCheckConfiguration::getName)
                .collect(Collectors.toSet());

        // find health checks that are registered but do not have a configured schedule
        final Set<String> notConfiguredHealthCheckNames = registeredHealthCheckNames.stream()
                .filter(healthCheckName -> !configuredHealthCheckNames.contains(healthCheckName))
                .collect(Collectors.toSet());

        if (!notConfiguredHealthCheckNames.isEmpty()) {
            final String healthCheckList = notConfiguredHealthCheckNames.stream()
                    .map(name -> "  * " + name)
                    .collect(Collectors.joining("\n"));
            log.info("The following health check(s) were registered, but are not configured with a schedule:\n{}",
                    healthCheckList);
        }

        // find health checks that are configured with a schedule but are not actually registered
        final Set<String> notRegisteredHealthCheckNames = configuredHealthCheckNames.stream()
                .filter(healthCheckName -> !registeredHealthCheckNames.contains(healthCheckName))
                .collect(Collectors.toSet());

        if (!notRegisteredHealthCheckNames.isEmpty()) {
            final String healthCheckList = notRegisteredHealthCheckNames.stream()
                    .map(name -> "  * " + name)
                    .collect(Collectors.joining("\n"));
            log.error("The following health check(s) are configured with a schedule, but were not registered:\n{}",
                    healthCheckList);
            throw new IllegalStateException("The following configured health checks were not registered: "
                    + notRegisteredHealthCheckNames);
        }
    }
}
