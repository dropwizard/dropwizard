package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeBoundHealthCheckTest {
    @Test
    void testCheck() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executorService = mock();
        final Duration duration = Duration.seconds(5);

        final Callable<HealthCheck.Result> callable = mock();
        final Future<HealthCheck.Result> future = mock();
        when(executorService.submit(callable)).thenReturn(future);

        new TimeBoundHealthCheck(executorService, duration).check(callable);
        verify(executorService, times(1)).submit(callable);
        verify(future, times(1)).get(duration.getQuantity(), duration.getUnit());
    }
}
