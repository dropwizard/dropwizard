package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.dropwizard.util.Maps;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class LogConfigurationTaskTest {

    private final LoggerContext loggerContext = new LoggerContext();
    private final Logger logger1 = loggerContext.getLogger("logger.one");
    private final Logger logger2 = loggerContext.getLogger("logger.two");

    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter output = new PrintWriter(stringWriter);

    private final LogConfigurationTask task = new LogConfigurationTask(loggerContext);

    @Test
    public void configuresSpecificLevelForALogger() throws Exception {

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

        assertThat(stringWriter.toString()).isEqualTo(String.format("Configured logging level for logger.one to DEBUG%n"));
    }

    @Test
    public void configuresSpecificLevelForALoggerForADuration() throws Exception {

        // given
        long millis = 2000;
        Level oneEffectiveBefore = logger1.getEffectiveLevel();
        Map<String, List<String>> parameters = Maps.of(
            "logger", Collections.singletonList("logger.one"),
            "level", Collections.singletonList("debug"),
            "duration", Collections.singletonList(Duration.ofMillis(millis).toString()));

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(stringWriter.toString()).isEqualTo(String.format("Configured logging level for logger.one to DEBUG for %d milliseconds%n", millis));

        // after
        Thread.sleep(4000);
        assertThat(logger1.getEffectiveLevel()).isEqualTo(oneEffectiveBefore);
    }

    @Test
    public void configuresDefaultLevelForALogger() throws Exception {
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

        assertThat(stringWriter.toString()).isEqualTo(String.format("Configured logging level for logger.one to null%n"));
    }

    @Test
    public void configuresLevelForMultipleLoggers() throws Exception {
        // given
        Map<String, List<String>> parameters = Maps.of(
                "logger", Arrays.asList("logger.one", "logger.two"),
                "level", Collections.singletonList("INFO"));

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getEffectiveLevel()).isEqualTo(Level.INFO);
        assertThat(logger2.getEffectiveLevel()).isEqualTo(Level.INFO);

        assertThat(stringWriter.toString())
                .isEqualTo(String.format("Configured logging level for logger.one to INFO%nConfigured logging level for logger.two to INFO%n"));
    }
}
