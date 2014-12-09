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
import org.quartz.jobs.NoOpJob;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuartzListJobsTaskTest
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

        JobDetail job1 = JobBuilder.newJob(NoOpJob.class)
                .withIdentity("foo", "bar")
                .storeDurably()
                .build();

        JobDetail job2 = JobBuilder.newJob(NoOpJob.class)
                .withIdentity("baz", "qux")
                .storeDurably()
                .build();

        Scheduler scheduler = bundle.getScheduler();
        scheduler.addJob(job1, true);
        scheduler.addJob(job2, true);
    }

    @Test
    public void testEnumAll() throws Exception
    {
        QuartzListJobsTask task = new QuartzListJobsTask(bundle, environment.getObjectMapper());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        task.execute(null, printWriter);

        assertThat(stringWriter.toString().length()).isGreaterThan(230);
    }

    @Test
    public void testEnumSpecificGroup() throws Exception
    {
        QuartzListJobsTask task = new QuartzListJobsTask(bundle, environment.getObjectMapper());

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        task.execute(new ImmutableMultimap.Builder<String, String>()
                .put("groupNames", "bar,quux")
                .build(), printWriter);

        assertThat(stringWriter.toString().length()).isGreaterThan(200);
    }
}
