package io.dropwizard.servlets.tasks;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.common.collect.ImmutableMultimap;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;

public class LogConfigurationTaskTest {

    private static final Level DEFAULT_LEVEL = Level.ALL;

    private final LoggerContext loggerContext = new LoggerContext();
    private final Logger logger1 = loggerContext.getLogger("logger.one");
    private final Logger logger2 = loggerContext.getLogger("logger.two");

    private final StringWriter stringWriter = new StringWriter();
    private final PrintWriter output = new PrintWriter(stringWriter);

    private final LogConfigurationTask task = new LogConfigurationTask(loggerContext);

    @Before
    public void setUp() throws Exception {
        logger1.setLevel(DEFAULT_LEVEL);
        logger2.setLevel(DEFAULT_LEVEL);
    }

    @Test
    public void configuresSpecificLevelForALogger() throws Exception {
        // given
        ImmutableMultimap<String, String> parameters = ImmutableMultimap.of(
                "logger", "logger.one",
                "level", "debug");

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(logger2.getLevel()).isEqualTo(DEFAULT_LEVEL);

        assertThat(stringWriter.toString()).isEqualTo(String.format("Configured logging level for logger.one to DEBUG%n"));
    }

    @Test
    public void configuresDefaultLevelForALogger() throws Exception {
        // given
        ImmutableMultimap<String, String> parameters = ImmutableMultimap.of(
                "logger", "logger.one");

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isNull();
        assertThat(logger2.getLevel()).isEqualTo(DEFAULT_LEVEL);

        assertThat(stringWriter.toString()).isEqualTo(String.format("Configured logging level for logger.one to null%n"));
    }

    @Test
    public void configuresLevelForMultipleLoggers() throws Exception {
        // given
        ImmutableMultimap<String, String> parameters = ImmutableMultimap.of(
                "logger", "logger.one",
                "logger", "logger.two",
                "level", "INFO");

        // when
        task.execute(parameters, output);

        // then
        assertThat(logger1.getLevel()).isEqualTo(Level.INFO);
        assertThat(logger2.getLevel()).isEqualTo(Level.INFO);

        assertThat(stringWriter.toString())
                .isEqualTo(String.format("Configured logging level for logger.one to INFO%nConfigured logging level for logger.two to INFO%n"));
    }
}
