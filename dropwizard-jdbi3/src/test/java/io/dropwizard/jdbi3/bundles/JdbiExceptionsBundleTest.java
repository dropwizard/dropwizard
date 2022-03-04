package io.dropwizard.jdbi3.bundles;

import io.dropwizard.core.Configuration;
import io.dropwizard.jdbi3.jersey.LoggingJdbiExceptionMapper;
import io.dropwizard.jdbi3.jersey.LoggingSQLExceptionMapper;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.core.setup.Environment;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbiExceptionsBundleTest {

    @Test
    void test() {
        Environment environment = mock(Environment.class);
        JerseyEnvironment jerseyEnvironment = mock(JerseyEnvironment.class);
        when(environment.jersey()).thenReturn(jerseyEnvironment);

        new JdbiExceptionsBundle().run(new Configuration(), environment);

        verify(jerseyEnvironment).register(isA(LoggingSQLExceptionMapper.class));
        verify(jerseyEnvironment).register(isA(LoggingJdbiExceptionMapper.class));
    }
}
