package io.dropwizard.jdbi.bundles;

import io.dropwizard.jdbi.jersey.LoggingDBIExceptionMapper;
import io.dropwizard.jdbi.jersey.LoggingSQLExceptionMapper;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Test;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DBIExceptionsBundleTest {

    @Test
    public void test() {
        Environment environment = mock(Environment.class);
        JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        new DBIExceptionsBundle().run(environment);

        verify(jerseyEnvironment, times(1)).register(isA(LoggingSQLExceptionMapper.class));
        verify(jerseyEnvironment, times(1)).register(isA(LoggingDBIExceptionMapper.class));
    }
}
