package io.dropwizard.documentation;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.documentation.config.MyConfiguration;
import io.dropwizard.documentation.resources.MyResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// core: MyApplicationTest
@ExtendWith(MockitoExtension.class)
class MyApplicationTest {
    @Mock
    private Environment environment;
    @Mock
    private JerseyEnvironment jersey;
    private MyApplication application;
    private MyConfiguration config;

    @BeforeEach
    void setup() throws Exception {
        config = new MyConfiguration();
        config.setMyParam("yay");
        application = new MyApplication();
        when(environment.jersey()).thenReturn(jersey);
    }

    @Test
    void buildsMyResource() throws Exception {
        application.run(config, environment);

        verify(jersey).register(eq(MyResource.class));
    }
}
// core: MyApplicationTest
