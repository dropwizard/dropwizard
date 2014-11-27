package io.dropwizard.quartz;

import com.codahale.metrics.health.HealthCheck;
import org.quartz.SchedulerFactory;

public class QuartzHealthCheck extends HealthCheck
{
    private final SchedulerFactory schedulerFactory;

    public QuartzHealthCheck(SchedulerFactory schedulerFactory)
    {
        this.schedulerFactory = schedulerFactory;
    }

    @Override
    protected Result check() throws Exception
    {
        if (schedulerFactory == null)
        {
            return Result.unhealthy("Scheduler Factory is not properly configured");
        } else
        {
            return Result.healthy();
        }
    }
}

