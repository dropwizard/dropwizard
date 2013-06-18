package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SyslogAppenderFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(SyslogAppenderFactory.class);
    }

    @Test
    public void defaultIncludesAppName() throws Exception {
        assertThat(new SyslogAppenderFactory().getLogFormat())
                .contains("%app");
    }

    @Test
    public void defaultIncludesPid() throws Exception {
        assertThat(new SyslogAppenderFactory().getLogFormat())
                .contains("%pid");
    }

    @Test
    public void patternIncludesAppNameAndPid() throws Exception {
        SyslogAppender appender = (SyslogAppender) new SyslogAppenderFactory()
                .build(new LoggerContext(), "MyApplication", null);
        assertThat(appender.getSuffixPattern())
                .matches("^MyApplication\\[\\d+\\].+");
    }
}
