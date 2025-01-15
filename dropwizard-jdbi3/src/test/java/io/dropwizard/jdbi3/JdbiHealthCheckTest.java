package io.dropwizard.jdbi3;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.jspecify.annotations.Nullable;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbiHealthCheckTest {
    private static final String VALIDATION_QUERY = "select 1";

    private Jdbi jdbi;
    private Handle handle;
    private Connection connection;
    private ExecutorService executorService;

    @BeforeEach
    void setup() {
        jdbi = mock(Jdbi.class);
        handle = mock(Handle.class);
        connection = mock(Connection.class);

        when(jdbi.open()).thenReturn(handle);
        when(handle.getConnection()).thenReturn(connection);

        executorService = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void teardown() {
        executorService.shutdown();
    }

    @Test
    void testNoTimeoutReturnsHealthy() throws Exception {
        when(handle.execute(VALIDATION_QUERY)).thenReturn(0);

        HealthCheck.Result result = healthCheck(VALIDATION_QUERY).check();

        assertThat(result.isHealthy()).isTrue();
    }

    @Test
    void tesHealthyAfterWhenMissingValidationQuery() throws Exception {
        when(connection.isValid(anyInt())).thenReturn(true);

        HealthCheck.Result result = healthCheck().check();

        assertThat(result.isHealthy()).isTrue();
        verify(connection).isValid(anyInt());
    }

    @Test
    void testItTimesOutProperly() throws Exception {
        when(handle.execute(VALIDATION_QUERY)).thenAnswer((Answer<Integer>) invocation -> {
            TimeUnit.SECONDS.sleep(10);
            return null;
        });

        HealthCheck.Result result = healthCheck(VALIDATION_QUERY).check();

        assertThat(result.isHealthy()).isFalse();
    }

    @Test
    void testUnhealthyWhenMissingValidationQuery() throws Exception {
        HealthCheck.Result result = healthCheck().check();

        assertThat(result.isHealthy()).isFalse();
        verify(connection).isValid(anyInt());
    }

    private JdbiHealthCheck healthCheck() {
        return healthCheck(null);
    }

    private JdbiHealthCheck healthCheck(@Nullable String validationQuery) {
        return new JdbiHealthCheck(executorService,
            Duration.milliseconds(100),
            jdbi,
            Optional.ofNullable(validationQuery));
    }
}
