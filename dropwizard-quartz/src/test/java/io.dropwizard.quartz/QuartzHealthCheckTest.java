package io.dropwizard.quartz;

import com.codahale.metrics.health.HealthCheckRegistry;
import io.dropwizard.Configuration;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuartzHealthCheckTest
{
    private final Configuration configuration = mock(Configuration.class);

    private final Environment environment = mock(Environment.class);

    private final LifecycleEnvironment mockLifecycleEnvironment = mock(LifecycleEnvironment.class);

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception
    {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.lifecycle()).thenReturn(mockLifecycleEnvironment);
    }

    @Test
    public void registersHealthCheck() throws Exception
    {
        final QuartzFactory quartzConfig = new QuartzFactory();
        QuartzBundle<Configuration> bundle = new QuartzBundle<Configuration>()
        {
            @Override
            public QuartzFactory getQuartzFactory(Configuration configuration)
            {
                return quartzConfig;
            }
        };

        bundle.run(configuration, environment);

        final ArgumentCaptor<QuartzHealthCheck> captor =
                ArgumentCaptor.forClass(QuartzHealthCheck.class);
        verify(healthChecks).register(eq("quartz"), captor.capture());
    }
}
