package io.dropwizard.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import org.junit.jupiter.api.Test;

import javax.validation.Validator;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultHealthFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<DefaultHealthFactory> configFactory =
        new YamlConfigurationFactory<>(DefaultHealthFactory.class, validator, objectMapper, "dw");

    @Test
    void shouldBuildHealthFactoryFromYaml() throws Exception {
        final DefaultHealthFactory healthFactory = configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/health.yml");

        assertThat(healthFactory.isDelayedShutdownHandlerEnabled()).isTrue();
        assertThat(healthFactory.isEnabled()).isTrue();
        assertThat(healthFactory.isInitialOverallState()).isTrue();
        assertThat(healthFactory.getShutdownWaitPeriod().toMilliseconds()).isEqualTo(1L);
        assertThat(healthFactory.getHealthCheckUrlPaths()).isEqualTo(singletonList("/health-check"));

        assertThat(healthFactory.getHealthChecks()).isEqualTo(healthFactory.getHealthCheckConfigurations());

        assertThat(healthFactory.getHealthCheckConfigurations()
            .stream()
            .map(HealthCheckConfiguration::getName)
            .collect(Collectors.toList()))
            .contains("foundationdb", "kafka", "redis");
        assertThat(healthFactory.getHealthCheckConfigurations()
            .stream()
            .map(HealthCheckConfiguration::isCritical)
            .collect(Collectors.toList()))
            .contains(true, false, false);
        healthFactory.getHealthCheckConfigurations().forEach(healthCheckConfig -> {
            assertThat(healthCheckConfig.getSchedule().getCheckInterval().toSeconds()).isEqualTo(5L);
            assertThat(healthCheckConfig.getSchedule().getDowntimeInterval().toSeconds()).isEqualTo(30L);
            assertThat(healthCheckConfig.getSchedule().getFailureAttempts()).isEqualTo(3);
            assertThat(healthCheckConfig.getSchedule().getSuccessAttempts()).isEqualTo(2);
        });
    }
}
