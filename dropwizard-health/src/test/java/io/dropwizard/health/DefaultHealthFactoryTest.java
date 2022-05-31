package io.dropwizard.health;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.response.ServletHealthResponder;
import io.dropwizard.health.response.ServletHealthResponderFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.ExecutorServiceManager;
import io.dropwizard.lifecycle.JettyManaged;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Test
    void configure() throws Exception {
        final DefaultHealthFactory healthFactory = configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/health.yml");

        LifecycleEnvironment lifecycleEnvironment = new LifecycleEnvironment(new MetricRegistry());

        ServletRegistration.Dynamic mockServletRegistration = mock(ServletRegistration.Dynamic.class);
        ServletEnvironment servletEnvironment = mock(ServletEnvironment.class);
        ArgumentCaptor<Servlet> servletCaptor = ArgumentCaptor.forClass(Servlet.class);
        when(servletEnvironment.addServlet(eq("health-check-test-servlet"), any(Servlet.class)))
            .thenReturn(mockServletRegistration);

        HealthEnvironment healthEnvironment = new HealthEnvironment(mock(HealthCheckRegistry.class));

        healthFactory.configure(
            lifecycleEnvironment,
            servletEnvironment,
            mock(JerseyEnvironment.class),
            healthEnvironment,
            new ObjectMapper(),
            "test");

        assertThat(lifecycleEnvironment.getManagedObjects())
            .hasSize(2)
            .allSatisfy(obj -> assertThat(obj).isInstanceOf(JettyManaged.class))
            .map(managed -> ((JettyManaged)managed).getManaged())
            .satisfies(obj -> assertThat(obj).element(0).isInstanceOfSatisfying(ExecutorServiceManager.class, executorServiceManager ->
                assertThat(executorServiceManager.getPoolName()).isEqualTo("health-check-test-scheduled-executor")))
            .satisfies(obj -> assertThat(obj).element(1).isInstanceOf(HealthCheckConfigValidator.class));

        assertThat(healthFactory.getHealthResponderFactory())
            .isInstanceOf(ServletHealthResponderFactory.class);

        verify(servletEnvironment).addServlet(eq("health-check-test-servlet"), servletCaptor.capture());
        assertThat(servletCaptor.getValue()).isInstanceOf(ServletHealthResponder.class);

        verify(mockServletRegistration).addMapping("/health-check");

        assertThat(healthEnvironment.healthStateAggregator()).isNotNull();
    }
}
