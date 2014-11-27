package io.dropwizard.quartz;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class QuartzBundleTest
{
    private final QuartzFactory quartzConfig = new QuartzFactory();

    private final Configuration configuration = mock(Configuration.class);

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);

    private final JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);

    private final LifecycleEnvironment mockLifecycleEnvironment = mock(LifecycleEnvironment.class);

    private final Environment environment = mock(Environment.class);

    private final QuartzBundle<Configuration> bundle = new QuartzBundle<Configuration>()
    {
        @Override
        public QuartzFactory getQuartzFactory(Configuration configuration)
        {
            return quartzConfig;
        }
    };

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        when(environment.lifecycle()).thenReturn(mockLifecycleEnvironment);
    }

    @Test
    public void testDefaultConfiguration() throws Exception
    {
        bundle.run(configuration, environment);
        assertThat(bundle.getSchedulerFactory()).isNotNull();
    }
}

