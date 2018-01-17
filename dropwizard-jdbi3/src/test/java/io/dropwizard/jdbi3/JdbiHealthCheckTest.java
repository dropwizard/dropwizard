package io.dropwizard.jdbi3;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbiHealthCheckTest {

    @Test
    public void testItTimesOutProperly() throws Exception {
        String validationQuery = "select 1";
        Jdbi jdbi = mock(Jdbi.class);
        Handle handle = mock(Handle.class);
        when(jdbi.open()).thenReturn(handle);
        when(jdbi.withHandle(Mockito.any())).thenCallRealMethod();
        Mockito.doAnswer(invocation -> {
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (Exception ignored) {
            }
            return null;
        }).when(handle).execute(validationQuery);

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        JdbiHealthCheck jdbiHealthCheck = new JdbiHealthCheck(executorService,
                Duration.milliseconds(5),
                jdbi,
                validationQuery);
        HealthCheck.Result result = jdbiHealthCheck.check();
        executorService.shutdown();
        assertThat("is unhealthy", false, is(result.isHealthy()));
    }
}
