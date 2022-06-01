package io.dropwizard.metrics.common;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.codahale.metrics.ScheduledReporter;
import io.dropwizard.util.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class ScheduledReporterManagerTest {

    @Test
    void testStopWithoutReporting() throws Exception {
        final boolean reportOnStop = false;
        ScheduledReporter mockReporter = mock(ScheduledReporter.class);
        ScheduledReporterManager manager =
                new ScheduledReporterManager(mockReporter, Duration.minutes(5), reportOnStop);

        manager.start();
        manager.stop();

        verify(mockReporter).start(5L, TimeUnit.MINUTES);
        verify(mockReporter).stop();
        verifyNoMoreInteractions(mockReporter);
    }

    @Test
    void testStopWithReporting() throws Exception {
        final boolean reportOnStop = true;
        ScheduledReporter mockReporter = mock(ScheduledReporter.class);
        ScheduledReporterManager manager =
                new ScheduledReporterManager(mockReporter, Duration.minutes(5), reportOnStop);

        manager.start();
        manager.stop();

        verify(mockReporter).start(5L, TimeUnit.MINUTES);
        verify(mockReporter).report();
        verify(mockReporter).stop();
        verifyNoMoreInteractions(mockReporter);
    }
}
