package io.dropwizard.jdbi3;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbiHealthCheckTest {
    private static final String VALIDATION_QUERY = "select 1";

    private Jdbi jdbi;
    private Handle handle;
    private ExecutorService executorService;
    private JdbiHealthCheck sut;

    @Before
    public void setup() {
        jdbi = mock(Jdbi.class);
        handle = mock(Handle.class);

        when(jdbi.open()).thenReturn(handle);

        executorService = Executors.newSingleThreadExecutor();

        sut = new JdbiHealthCheck(executorService,
            Duration.milliseconds(100),
            jdbi,
            VALIDATION_QUERY);
    }

    @After
    public void teardown() {
        executorService.shutdown();
    }

    @Test
    public void testNoTimeoutReturnsHealthy() throws Exception {
        when(handle.execute(VALIDATION_QUERY)).thenReturn(0);

        HealthCheck.Result result = sut.check();

        assertThat(result.isHealthy()).isTrue();
    }

    @Test
    public void testItTimesOutProperly() throws Exception {
        when(handle.execute(VALIDATION_QUERY)).thenAnswer((Answer<Integer>) invocation -> {
            TimeUnit.SECONDS.sleep(10);
            return null;
        });

        HealthCheck.Result result = sut.check();

        assertThat(result.isHealthy()).isFalse();
    }
}
