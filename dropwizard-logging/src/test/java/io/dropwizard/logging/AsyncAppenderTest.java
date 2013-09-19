package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.util.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class AsyncAppenderTest {
    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> delegate = mock(Appender.class);
    private final AsyncAppender appender = new AsyncAppender(delegate, 100, Duration.milliseconds(100), true);

    @Before
    public void setUp() throws Exception {
        appender.start();
    }

    @After
    public void tearDown() throws Exception {
        appender.stop();
    }

    @Test
    public void delegatesAppending() throws Exception {
        final ILoggingEvent event = mock(ILoggingEvent.class);
        appender.append(event);

        verify(delegate, timeout(200)).doAppend(event);
    }
}
