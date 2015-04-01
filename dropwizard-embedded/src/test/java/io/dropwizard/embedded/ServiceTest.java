package io.dropwizard.embedded;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ServiceTest {
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    private class PingResource {
        @GET
        public String ping() {
            return "pong";
        }
    }

    private @Spy Service<Configuration> spyService = new Service<Configuration>(null) {
        @Override
        public void initialize(EmbeddedBootstrap<Configuration> bootstrap) {
            assertThat(bootstrap).isNotNull();
        }

        @Override
        protected void start(Configuration configuration, Environment environment) throws Exception {
            assertThat(configuration).isNotNull();
            assertThat(environment).isNotNull();

            environment.jersey().register(new PingResource());
        }
    };

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void serviceStartsAndStops() throws Exception {
        spyService.start();
        verify(spyService).initialize(any(EmbeddedBootstrap.class));
        verify(spyService).start(any(Configuration.class), any(Environment.class));

        spyService.stop();
        verify(spyService).stop(any(Environment.class));
    }

    @Test
    public void serviceDoesNotStopIfNotStarted() throws Exception {
        spyService.stop();
        verify(spyService, never()).initialize(any(EmbeddedBootstrap.class));
        verify(spyService, never()).start(any(Configuration.class), any(Environment.class));
        verify(spyService, never()).stop(any(Environment.class));
    }
}
