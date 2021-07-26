package io.dropwizard.health;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.health.conf.HealthCheckConfiguration;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class HealthCheckConfigValidatorTest {

    @Mock
    private Appender<ILoggingEvent> mockLogAppender;

    @BeforeEach
    public void setUp() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(HealthCheckConfigValidator.class);
        logger.addAppender(mockLogAppender);
    }

    @AfterEach
    public void tearDown() throws Exception {
        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory
                .getLogger(HealthCheckConfigValidator.class);
        logger.detachAppender(mockLogAppender);
        MDC.clear();
    }

    @Test
    public void startValidationsShouldSucceedWhenNoHealthChecksConfigured() throws Exception {
        // given
        List<HealthCheckConfiguration> configs = ImmutableList.of();
        HealthCheckRegistry registry = new HealthCheckRegistry();

        // when
        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(configs, registry);
        validator.start();

        // then
        verifyNoInteractions(mockLogAppender);
    }

    @Test
    public void startValidationsShouldSucceedForConfiguredAndRegisteredHealthCheck() throws Exception {
        // given
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        HealthCheckConfiguration check2 = new HealthCheckConfiguration();
        check2.setName("check-2");
        List<HealthCheckConfiguration> configs = ImmutableList.of(check1, check2);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("check-1", mock(HealthCheck.class));
        registry.register("check-2", mock(HealthCheck.class));

        // when
        HealthCheckConfigValidator validator = new HealthCheckConfigValidator(configs, registry);
        validator.start();

        // then
        verifyNoInteractions(mockLogAppender);
    }

    @Test
    public void startValidationsShouldSucceedButLogWhenNotAllHealthChecksAreConfigured() throws Exception {
        // given
        ArgumentCaptor<LoggingEvent> captor = ArgumentCaptor.forClass(LoggingEvent.class);
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        List<HealthCheckConfiguration> configs = ImmutableList.of(check1);
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
    public void startValidationsShouldFailIfAHealthCheckConfiguredButNotRegistered() throws Exception {
        // given
        ArgumentCaptor<LoggingEvent> captor = ArgumentCaptor.forClass(LoggingEvent.class);
        HealthCheckConfiguration check1 = new HealthCheckConfiguration();
        check1.setName("check-1");
        HealthCheckConfiguration check2 = new HealthCheckConfiguration();
        check2.setName("check-2");
        HealthCheckConfiguration check3 = new HealthCheckConfiguration();
        check3.setName("check-3");
        List<HealthCheckConfiguration> configs = ImmutableList.of(check1, check2, check3);
        HealthCheckRegistry registry = new HealthCheckRegistry();
        registry.register("check-1", mock(HealthCheck.class));

        // when
        try {
            HealthCheckConfigValidator validator = new HealthCheckConfigValidator(configs, registry);
            validator.start();
            fail("configured health checks that aren't registered should fail");
        } catch (IllegalStateException e) {
            // then
            verify(mockLogAppender).doAppend(captor.capture());
            LoggingEvent logEvent = captor.getValue();
            assertThat(logEvent.getLevel())
                    .isEqualTo(Level.ERROR);
            assertThat(logEvent.getFormattedMessage())
                    .doesNotContain("  * check-1");
            assertThat(logEvent.getFormattedMessage())
                    .contains("  * check-3");
            assertThat(logEvent.getFormattedMessage())
                    .contains("  * check-3");
            assertThat(e.getMessage())
                    .contains("[check-3, check-2]");
        }
    }
}
