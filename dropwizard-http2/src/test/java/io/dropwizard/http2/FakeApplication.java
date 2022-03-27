package io.dropwizard.http2;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

public class FakeApplication extends Application<Configuration> {

    public static final String HELLO_WORLD = "{\"hello\": \"World\"}";

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        environment.jersey().register(new FakeResource());
        environment.healthChecks().register("fake-health-check", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });
    }

    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public static class FakeResource {

        @GET
        public String get() throws Exception {
            return HELLO_WORLD;
        }
    }
}
