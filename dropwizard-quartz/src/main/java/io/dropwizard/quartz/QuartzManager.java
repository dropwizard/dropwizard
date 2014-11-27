package io.dropwizard.quartz;

import io.dropwizard.lifecycle.Managed;
import org.quartz.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuartzManager implements Managed
{
    private static final Logger LOG = LoggerFactory.getLogger(QuartzManager.class);

    private final Scheduler scheduler;

    public QuartzManager(Scheduler scheduler)
    {
        this.scheduler = scheduler;
    }

    @Override
    public void start() throws Exception
    {
        LOG.info("Starting Quartz scheduler...");
        scheduler.start();
    }

    @Override
    public void stop() throws Exception
    {
        LOG.info("Shutting down Quartz scheduler (waiting for running jobs to complete)...");
        scheduler.shutdown(true);
    }
}
