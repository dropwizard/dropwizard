package io.dropwizard.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.conf.HealthCheckConfiguration;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.File;
import java.util.stream.Collectors;
import javax.validation.Validator;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class DefaultHealthFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<DefaultHealthFactory> configFactory =
            new YamlConfigurationFactory<>(DefaultHealthFactory.class, validator, objectMapper, "dw");

    @Test
    public void shouldBuildHealthFactoryFromYaml() throws Exception {
        final File yml = new File(Resources.getResource("yml/health.yml").toURI());
        final DefaultHealthFactory healthFactory = configFactory.build(yml);

        assertThat(healthFactory.isDelayedShutdownHandlerEnabled()).isTrue();
        assertThat(healthFactory.getShutdownWaitPeriod().toMilliseconds()).isEqualTo(1L);
        assertThat(healthFactory.getHealthCheckUrlPaths()).isEqualTo(ImmutableList.of("/health-check"));

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
