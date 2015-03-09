package io.dropwizard.db;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.junit.Test;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TimeBoundHealthCheckTest {
    
    @Test
    @SuppressWarnings("unchecked")
    public void testCheck() throws InterruptedException, ExecutionException, TimeoutException {
        final ExecutorService executorService = mock(ExecutorService.class);
        final Duration duration = mock(Duration.class);
        when(duration.getQuantity()).thenReturn(5L);
        when(duration.getUnit()).thenReturn(TimeUnit.SECONDS);
        
        final Callable<HealthCheck.Result> callable = mock(Callable.class);
        final Future<HealthCheck.Result> future = mock(Future.class);
        when(executorService.submit(callable)).thenReturn(future);

        new TimeBoundHealthCheck(executorService, duration).check(callable);
        verify(executorService, times(1)).submit(callable);
        verify(future, times(1)).get(duration.getQuantity(), duration.getUnit());
    }
}
