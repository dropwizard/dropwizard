package io.dropwizard.health;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = DefaultHealthFactory.class)
public interface HealthFactory extends Discoverable {
    void configure(LifecycleEnvironment lifecycle, ServletEnvironment servlets, JerseyEnvironment jersey,
                   HealthEnvironment health, ObjectMapper mapper, String name);
    boolean isOnAdminServlet();
}
