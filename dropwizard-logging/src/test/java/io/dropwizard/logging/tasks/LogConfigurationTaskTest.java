/*
 * Copyright 2014 Stuart Gunter
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package io.dropwizard.logging.tasks;

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

        assertThat(stringWriter.toString()).isEqualTo("Configured logging level for logger.one to DEBUG\n");
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

        assertThat(stringWriter.toString()).isEqualTo("Configured logging level for logger.one to null\n");
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

        assertThat(stringWriter.toString()).isEqualTo(
                "Configured logging level for logger.one to INFO\n" +
                        "Configured logging level for logger.two to INFO\n");
    }
}
