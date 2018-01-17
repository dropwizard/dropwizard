package io.dropwizard.jdbi3.bundles;

import io.dropwizard.jdbi3.jersey.LoggingJdbiExceptionMapper;
import io.dropwizard.jdbi3.jersey.LoggingSQLExceptionMapper;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Test;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JdbiExceptionsBundleTest {

    @Test
    public void test() {
        Environment environment = mock(Environment.class);
        JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        new JdbiExceptionsBundle().run(environment);

        verify(jerseyEnvironment).register(isA(LoggingSQLExceptionMapper.class));
        verify(jerseyEnvironment).register(isA(LoggingJdbiExceptionMapper.class));
    }
}
