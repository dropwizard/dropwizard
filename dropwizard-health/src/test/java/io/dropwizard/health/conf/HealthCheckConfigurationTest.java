package io.dropwizard.health.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.File;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class HealthCheckConfigurationTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<HealthCheckConfiguration> configFactory =
            new YamlConfigurationFactory<>(HealthCheckConfiguration.class, validator, objectMapper, "dw");

    @Test
    public void shouldBuildHealthCheckConfigurationFromYaml() throws Exception {
        final File yml = new File(Resources.getResource("yml/healthCheck.yml").toURI());
        final HealthCheckConfiguration healthCheckConfig = configFactory.build(yml);

        assertThat(healthCheckConfig.getName()).isEqualTo("cassandra");
        assertThat(healthCheckConfig.isCritical()).isTrue();
        assertThat(healthCheckConfig.getSchedule().getCheckInterval().toSeconds()).isEqualTo(5L);
        assertThat(healthCheckConfig.getSchedule().getDowntimeInterval().toSeconds()).isEqualTo(30L);
        assertThat(healthCheckConfig.getSchedule().getFailureAttempts()).isEqualTo(3);
        assertThat(healthCheckConfig.getSchedule().getSuccessAttempts()).isEqualTo(2);
    }
}
