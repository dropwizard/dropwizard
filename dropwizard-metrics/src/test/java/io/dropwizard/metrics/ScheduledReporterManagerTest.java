package io.dropwizard.metrics;

import com.codahale.metrics.ScheduledReporter;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

public class ScheduledReporterManagerTest {

    @Test
    public void testStopWithoutReporting() throws Exception {
        final boolean reportOnStop = false;
        ScheduledReporter mockReporter = Mockito.mock(ScheduledReporter.class);
        ScheduledReporterManager manager = new ScheduledReporterManager(mockReporter, Duration.minutes(5), reportOnStop);

        manager.start();
        manager.stop();

        Mockito.verify(mockReporter).start(Mockito.eq(5L), Mockito.eq(TimeUnit.MINUTES));
        Mockito.verify(mockReporter).stop();
        Mockito.verifyNoMoreInteractions(mockReporter);
    }

    @Test
    public void testStopWithReporting() throws Exception {
        final boolean reportOnStop = true;
        ScheduledReporter mockReporter = Mockito.mock(ScheduledReporter.class);
        ScheduledReporterManager manager = new ScheduledReporterManager(mockReporter, Duration.minutes(5), reportOnStop);

        manager.start();
        manager.stop();

        Mockito.verify(mockReporter).start(Mockito.eq(5L), Mockito.eq(TimeUnit.MINUTES));
        Mockito.verify(mockReporter).report();
        Mockito.verify(mockReporter).stop();
        Mockito.verifyNoMoreInteractions(mockReporter);
    }

}
