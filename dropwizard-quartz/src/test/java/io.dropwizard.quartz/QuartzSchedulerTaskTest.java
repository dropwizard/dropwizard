package io.dropwizard.quartz;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMultimap;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.AdminEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.jobs.NoOpJob;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuartzSchedulerTaskTest
{
    private final Configuration configuration = mock(Configuration.class);

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);

    private final AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);

    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);

    private final Environment environment = mock(Environment.class);

    private QuartzBundle<Configuration> bundle;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.admin()).thenReturn(adminEnvironment);
        when(environment.getObjectMapper()).thenReturn(new ObjectMapper());

        final QuartzFactory quartzConfig = new QuartzFactory();
        bundle = new QuartzBundle<Configuration>()
        {
            @Override
            public QuartzFactory getQuartzFactory(Configuration configuration)
            {
                return quartzConfig;
            }
        };

        bundle.run(configuration, environment);

        JobDetail job = JobBuilder.newJob(NoOpJob.class)
                .withIdentity("apple", "pear")
                .storeDurably()
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(30)
                        .repeatForever())
                .build();

        Scheduler scheduler = bundle.getSchedulerFactory().getScheduler();
        scheduler.scheduleJob(job, trigger);

        scheduler.start();

    }

    @Test
    public void testStateTransitions() throws Exception
    {
        QuartzSchedulerTask task = new QuartzSchedulerTask(bundle.getSchedulerFactory());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        task.execute(new ImmutableMultimap.Builder<String, String>()
                .put("action", "standby")
                .build(), printWriter);

        assertThat(bundle.getSchedulerFactory().getScheduler().isInStandbyMode()).isEqualTo(true);

        task.execute(new ImmutableMultimap.Builder<String, String>()
                .put("action", "start")
                .build(), printWriter);

        assertThat(bundle.getSchedulerFactory().getScheduler().isStarted()).isEqualTo(true);

        task.execute(new ImmutableMultimap.Builder<String, String>()
                .put("action", "pauseAll")
                .build(), printWriter);

        assertThat(bundle.getSchedulerFactory().getScheduler().getPausedTriggerGroups().size()).isGreaterThan(0);

        task.execute(new ImmutableMultimap.Builder<String, String>()
                .put("action", "resumeAll")
                .build(), printWriter);

        assertThat(bundle.getSchedulerFactory().getScheduler().getPausedTriggerGroups().size()).isEqualTo(0);
    }
}
