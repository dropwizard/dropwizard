package io.dropwizard.http2;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

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
