package io.dropwizard.quartz;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.quartz.SchedulerConfigException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuartzBundleJDBCJobStoreTXTest
{
    private final Configuration configuration = mock(Configuration.class);

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);

    private final LifecycleEnvironment mockLifecycleEnvironment = mock(LifecycleEnvironment.class);

    private final Environment environment = mock(Environment.class);

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.lifecycle()).thenReturn(mockLifecycleEnvironment);
    }

    @Test(expected = SchedulerConfigException.class)
    public void testBadJDBCConfiguration() throws Exception
    {
        final QuartzFactory quartzConfig = new QuartzFactory();
        QuartzJDBCJobStoreTX jdbcJobStoreTX = new QuartzJDBCJobStoreTX();

        // Missing Data Source Name - must throw an exception
        jdbcJobStoreTX.setDataSource(null);

        quartzConfig.setJDBCJobStoreTX(jdbcJobStoreTX);

        QuartzBundle<Configuration> bundle = new QuartzBundle<Configuration>()
        {
            @Override
            public QuartzFactory getQuartzFactory(Configuration configuration)
            {
                return quartzConfig;
            }
        };

        bundle.run(configuration, environment);
        assertThat(bundle.getSchedulerFactory()).isNotNull();
    }
}

