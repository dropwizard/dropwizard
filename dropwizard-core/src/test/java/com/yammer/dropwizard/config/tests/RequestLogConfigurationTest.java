package com.yammer.dropwizard.config.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.AppenderConfiguration;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.RequestLogConfiguration;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;

public class RequestLogConfigurationTest {
    private RequestLogConfiguration requestLog;

    @Before
    public void setUp() throws Exception {
        this.requestLog = ConfigurationFactory
                .forClass(RequestLogConfiguration.class, new Validator())
                .build(new File(Resources.getResource("yaml/requestLog.yml").toURI()));
    }

    @Test
    public void defaultTimeZoneIsUTC() {
        assertThat(requestLog.getTimeZone())
            .isEqualTo(TimeZone.getTimeZone("UTC"));
    }

    @Test
    public void fileConfigurationCanBeEnabled() throws Exception {
        assertThat(requestLog.getFileConfiguration().isEnabled())
            .isTrue();
    }

    @Test
    public void hasCustomConfiguration() throws Exception {
        final List<AppenderConfiguration> appenders = requestLog.getAppenderConfigurations();

        assertThat(appenders.size())
                .isEqualTo(1);

        final AppenderConfiguration appender = appenders.get(0);

        assertThat(appender.isEnabled())
                .isFalse();

        assertThat(appender.getThreshold())
                .isEqualTo(Level.ALL);

        assertThat(appender)
                .isInstanceOf(TestCustomLogging.class);

        assertThat(((TestCustomLogging)appender).customValue)
                .isEqualTo(18);
    }

    public static class TestCustomLogging extends AppenderConfiguration {
        @JsonProperty
        public int customValue = 10;

        @Override
        protected Appender<ILoggingEvent> createAppender(LoggerContext context, String name) {
            return new ConsoleAppender<ILoggingEvent>();
        }
    }
}
