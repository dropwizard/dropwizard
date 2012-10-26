package com.yammer.dropwizard.jetty.tests;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.yammer.dropwizard.jetty.AsyncRequestLog;
import org.junit.Before;
import org.junit.Test;

import java.util.TimeZone;

import static org.mockito.Mockito.*;

public class AsyncRequestLogTest {
    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> appender = mock(Appender.class);
    private final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<ILoggingEvent>();
    private final AsyncRequestLog asyncRequestLog = new AsyncRequestLog(appenders, TimeZone.getDefault());

    @Before
    public void setUp() throws Exception {
        appenders.addAppender(appender);
    }

    @Test
    public void startsAndStops() throws Exception {
        asyncRequestLog.start();
        asyncRequestLog.stop();

        verify(appender, timeout(1000)).stop();
    }
}
