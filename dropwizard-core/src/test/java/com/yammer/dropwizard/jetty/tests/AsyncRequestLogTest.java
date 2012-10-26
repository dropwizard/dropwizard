package com.yammer.dropwizard.jetty.tests;

import static org.mockito.Mockito.mock;

import java.util.TimeZone;

import org.junit.Assert;
import org.junit.Test;

import ch.qos.logback.core.spi.AppenderAttachableImpl;

import com.yammer.dropwizard.jetty.AsyncRequestLog;

public class AsyncRequestLogTest {
    private final AppenderAttachableImpl appender = mock(AppenderAttachableImpl.class);
    private final AsyncRequestLog asyncRequestLog = new AsyncRequestLog(appender, TimeZone.getDefault());

    @Test
    public void startsAndStops() throws Exception {
        asyncRequestLog.start();
        asyncRequestLog.stop();
        for (int i=0; i<100; i++) {
            if (!asyncRequestLog.isThreadAlive()) break;
            Thread.sleep(100);
        }
        Assert.assertFalse(asyncRequestLog.isThreadAlive());
    }
}
