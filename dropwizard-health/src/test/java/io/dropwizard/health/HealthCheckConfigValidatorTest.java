package io.dropwizard.health;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckConfigValidatorTest {

    @Mock
    private Appender<ILoggingEvent> mockLogAppender;

    @BeforeEach
    void setUp() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(HealthCheckConfigValidator.class);
        logger.addAppender(mockLogAppender);
    }

    @AfterEach
    void tearDown() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
            .getLogger(HealthCheckConfigValidator.class);
        logger.detachAppender(mockLogAppender);
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
        verifyNoInteractions(mockLogAppender);
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
        verifyNoInteractions(mockLogAppender);
    }

    @Test
    void startValidationsShouldSucceedButLogWhenNotAllHealthChecksAreConfigured() throws Exception {
        // given
        ArgumentCaptor<LoggingEvent> captor = ArgumentCaptor.forClass(LoggingEvent.class);
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
        verify(mockLogAppender).doAppend(captor.capture());
        LoggingEvent logEvent = captor.getValue();
        assertThat(logEvent.getLevel()).isEqualTo(Level.INFO);
        assertThat(logEvent.getFormattedMessage())
            .doesNotContain("  * check-1");
        assertThat(logEvent.getFormattedMessage())
            .contains("  * check-2");
        assertThat(logEvent.getFormattedMessage())
            .contains("  * check-3");
    }

    @Test
    void startValidationsShouldFailIfAHealthCheckConfiguredButNotRegistered() {
        // given
        ArgumentCaptor<LoggingEvent> captor = ArgumentCaptor.forClass(LoggingEvent.class);
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

        verify(mockLogAppender).doAppend(captor.capture());
        LoggingEvent logEvent = captor.getValue();
        assertThat(logEvent.getLevel())
            .isEqualTo(Level.ERROR);
        assertThat(logEvent.getFormattedMessage())
            .doesNotContain("  * check-1")
            .contains("  * check-2\n  * check-3");
    }
}
