package io.dropwizard.health;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.*;

class HealthCheckConfigValidatorTest {
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    @BeforeEach
    @SuppressWarnings("Slf4jIllegalPassedClass")
    void setUp() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(HealthCheckConfigValidator.class);
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @AfterEach
    @SuppressWarnings("Slf4jIllegalPassedClass")
    void tearDown() {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(HealthCheckConfigValidator.class);
        logger.detachAppender(listAppender);
        MDC.clear();
    }

    @Test
    void startValidationsShouldSucceedWhenNoHealthChecksConfigured() throws Exception {
        // given
        List<HealthCheckConfiguration> configs = emptyList();
        HealthCheckRegistry registry = new HealthCheckRegistry();

        // when
        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(configs, registry);
        validator.start();

        // then
        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void startValidationsShouldSucceedForConfiguredAndRegisteredHealthCheck() throws Exception {
        // given
        List<HealthCheckConfiguration> configs = new ArrayList<>();
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        configs.add(check1);
        HealthCheckConfiguration check2 = new HealthCheckConfiguration();
        check2.setName("check-2");
        configs.add(check2);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("check-1", mock(HealthCheck.class));
        registry.register("check-2", mock(HealthCheck.class));

        // when
        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(unmodifiableList(configs), registry);
        validator.start();

        // then
        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void startValidationsShouldSucceedButLogWhenNotAllHealthChecksAreConfigured() throws Exception {
        // given
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        List<HealthCheckConfiguration> configs = singletonList(check1);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("check-1", mock(HealthCheck.class));
        registry.register("check-2", mock(HealthCheck.class));
        registry.register("check-3", mock(HealthCheck.class));

        // when
        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(configs, registry);
        validator.start();

        // then
        assertThat(listAppender.list)
            .singleElement()
            .satisfies(logEvent -> assertThat(logEvent.getLevel()).isEqualTo(Level.INFO))
            .satisfies(logEvent -> assertThat(logEvent.getFormattedMessage())
                .doesNotContain("  * check-1")
                .contains("  * check-2")
                .contains("  * check-3"));
    }

    @Test
    void startValidationsShouldFailIfAHealthCheckConfiguredButNotRegistered() {
        // given
        List<HealthCheckConfiguration> configs = new ArrayList<>();
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        configs.add(check1);
        HealthCheckConfiguration check2 = new HealthCheckConfiguration();
        check2.setName("check-2");
        configs.add(check2);
        HealthCheckConfiguration check3 = new HealthCheckConfiguration();
        check3.setName("check-3");
        configs.add(check3);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("check-1", mock(HealthCheck.class));

        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(unmodifiableList(configs), registry);
        assertThatIllegalStateException()
            .isThrownBy(validator::start)
            .withMessageContaining("[check-2, check-3]");

        assertThat(listAppender.list)
            .singleElement()
            .satisfies(logEvent -> assertThat(logEvent.getLevel()).isEqualTo(Level.ERROR))
            .satisfies(logEvent -> assertThat(logEvent.getFormattedMessage())
                .doesNotContain("  * check-1")
                .contains("  * check-2\n  * check-3"));
    }
}
