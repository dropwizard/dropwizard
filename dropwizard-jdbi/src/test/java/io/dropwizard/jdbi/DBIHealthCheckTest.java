package io.dropwizard.jdbi;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.util.Duration;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DBIHealthCheckTest {

    @Test 
    public void testItTimesOutProperly() throws Exception {
        String validationQuery = "select 1";
        DBI dbi = mock(DBI.class);
        Handle handle = mock(Handle.class);
        when(dbi.open()).thenReturn(handle);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                try {
                    TimeUnit.SECONDS.sleep(10);
                }
                catch (Exception ignored) {}
                return null;
            }
        }).when(handle).execute(validationQuery);
        DBIHealthCheck dbiHealthCheck = new DBIHealthCheck(Executors.newSingleThreadExecutor(), 
                Duration.milliseconds(5),
                dbi, 
                validationQuery);
        HealthCheck.Result result = dbiHealthCheck.check();
        assertThat("is unhealthy", false, is(result.isHealthy()));
    }
}
