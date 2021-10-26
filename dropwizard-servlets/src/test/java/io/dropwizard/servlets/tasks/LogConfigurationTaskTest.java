package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.util.Maps;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogConfigurationTaskTest {

    private final LoggerContext loggerContext = new LoggerContext();
    private final Logger logger1 = loggerContext.getLogger("logger.one");
    private final Logger logger2 = loggerContext.getLogger("logger.two");

    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter output = new PrintWriter(stringWriter);

    private final LogConfigurationTask task = new LogConfigurationTask(loggerContext);

    @Test
    void configuresSpecificLevelForALogger() throws Exception {

        // given
        Level twoEffectiveBefore = logger2.getEffectiveLevel();
        Map<String, List<String>> parameters = Maps.of(
                "logger", Collections.singletonList("logger.one"),
                "level", Collections.singletonList("debug"));

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getEffectiveLevel()).isEqualTo(Level.DEBUG);
        assertThat(logger2.getEffectiveLevel()).isEqualTo(twoEffectiveBefore);

        assertThat(stringWriter).hasToString(String.format("Configured logging level for logger.one to DEBUG%n"));
    }

    @Test
    void configuresSpecificLevelForALoggerForADuration() throws Exception {

        // given
        Level oneEffectiveBefore = logger1.getEffectiveLevel();
        Map<String, List<String>> parameters = Maps.of(
            "logger", Collections.singletonList("logger.one"),
            "level", Collections.singletonList("debug"),
            "duration", Collections.singletonList(Duration.ofMillis(2_000).toString()));

        Timer timer = mock(Timer.class);
        ArgumentCaptor<TimerTask> timerAction = ArgumentCaptor.forClass(TimerTask.class);
        ArgumentCaptor<Long> timerDuration = ArgumentCaptor.forClass(Long.class);

        // when
        new LogConfigurationTask(loggerContext, () -> timer).execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(stringWriter).hasToString(String.format("Configured logging level for logger.one to DEBUG for 2000 milliseconds%n"));
        verify(timer).schedule(timerAction.capture(), timerDuration.capture());
        assertThat(timerDuration.getValue()).isEqualTo(2_000);

        // after
        timerAction.getValue().run();
        assertThat(logger1.getEffectiveLevel()).isEqualTo(oneEffectiveBefore);
    }

    @Test
    void configuresDefaultLevelForALogger() throws Exception {
        // given
        Level oneEffectiveBefore = logger1.getEffectiveLevel();
        Level twoEffectiveBefore = logger2.getEffectiveLevel();
        Map<String, List<String>> parameters = Collections.singletonMap("logger", Collections.singletonList("logger.one"));

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isNull();
        assertThat(logger1.getEffectiveLevel()).isEqualTo(oneEffectiveBefore);
        assertThat(logger2.getEffectiveLevel()).isEqualTo(twoEffectiveBefore);

        assertThat(stringWriter).hasToString(String.format("Configured logging level for logger.one to null%n"));
    }

    @Test
    void configuresLevelForMultipleLoggers() throws Exception {
        // given
        Map<String, List<String>> parameters = Maps.of(
                "logger", Arrays.asList("logger.one", "logger.two"),
                "level", Collections.singletonList("INFO"));

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getEffectiveLevel()).isEqualTo(Level.INFO);
        assertThat(logger2.getEffectiveLevel()).isEqualTo(Level.INFO);

        assertThat(stringWriter)
                .hasToString(String.format("Configured logging level for logger.one to INFO%nConfigured logging level for logger.two to INFO%n"));
    }
}
