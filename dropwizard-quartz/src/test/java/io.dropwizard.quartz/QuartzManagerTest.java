package io.dropwizard.quartz;

import org.junit.Test;
import org.quartz.Scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class QuartzManagerTest
{
    private final Scheduler scheduler = mock(Scheduler.class);

    private final QuartzManager manager = new QuartzManager(scheduler);

    @Test
    public void shutdownTheSchedulerOnStopping() throws Exception
    {
        manager.stop();
        verify(scheduler).shutdown(any(Boolean.class));
    }
}
