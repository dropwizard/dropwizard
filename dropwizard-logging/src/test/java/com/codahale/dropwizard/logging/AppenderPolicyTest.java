package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.helpers.NOPAppender;
import org.junit.Test;

import static com.codahale.dropwizard.logging.AppenderPolicy.*;
import static org.fest.assertions.api.Assertions.assertThat;

public class AppenderPolicyTest {

    private final Appender<ILoggingEvent> testAppender = new NOPAppender<>();

    @Test
    public void checkAsyncDroppingWithCallee() throws Exception {
        AsyncAppender appender =
            (AsyncAppender) ASYNC_DROPPING_FULL_CALLEE.wrapAppenderIfNecessary(testAppender, 100);

        assertThat(appender.getDiscardingThreshold()).isPositive();
        assertThat(appender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void checkAsyncDroppingWithoutCallee() throws Exception {
        AsyncAppender appender =
            (AsyncAppender) ASYNC_DROPPING.wrapAppenderIfNecessary(testAppender, 100);

        assertThat(appender.getDiscardingThreshold()).isPositive();
        assertThat(appender.isIncludeCallerData()).isFalse();
    }

    @Test
    public void checkAsyncStallingWithCallee() throws Exception {
        AsyncAppender appender =
            (AsyncAppender) ASYNC_STALLING_FULL_CALLEE.wrapAppenderIfNecessary(testAppender, 100);

        assertThat(appender.getDiscardingThreshold()).isZero();
        assertThat(appender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void checkAsyncStallingWithoutCallee() throws Exception {
        AsyncAppender appender =
            (AsyncAppender) ASYNC_STALLING.wrapAppenderIfNecessary(testAppender, 100);

        assertThat(appender.getDiscardingThreshold()).isZero();
        assertThat(appender.isIncludeCallerData()).isFalse();
    }

    @Test
    public void checkSync() throws Exception {
        Appender appender = SYNC.wrapAppenderIfNecessary(testAppender, 100);
        assertThat(appender).isSameAs(testAppender);
    }

}
