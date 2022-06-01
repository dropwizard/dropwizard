package io.dropwizard.health;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;

class HealthCheckConfigurationTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<HealthCheckConfiguration> configFactory =
            new YamlConfigurationFactory<>(HealthCheckConfiguration.class, validator, objectMapper, "dw");

    @Test
    void shouldBuildHealthCheckConfigurationFromYaml() throws Exception {
        final HealthCheckConfiguration healthCheckConfig =
                configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/healthCheck.yml");

        assertThat(healthCheckConfig.getName()).isEqualTo("cassandra");
        assertThat(healthCheckConfig.isCritical()).isTrue();
        assertThat(healthCheckConfig.getSchedule().getCheckInterval().toSeconds())
                .isEqualTo(5L);
        assertThat(healthCheckConfig.getSchedule().getDowntimeInterval().toSeconds())
                .isEqualTo(30L);
        assertThat(healthCheckConfig.getSchedule().getFailureAttempts()).isEqualTo(3);
        assertThat(healthCheckConfig.getSchedule().getSuccessAttempts()).isEqualTo(2);
    }
}
