package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;
import org.glassfish.jersey.server.model.Resource;

import java.util.Collection;

@JsonTypeName("jersey")
public class JerseyHealthResponderFactory implements HealthResponderFactory {
    private static final String PATH_TEMPLATE = "/{a:%s}";
    private static final String PATH_REGEX_DELIMITER = "|";

    @JsonProperty
    private boolean cacheControlEnabled = true;

    @JsonProperty
    private String cacheControlValue = "no-store";

    public boolean isCacheControlEnabled() {
        return cacheControlEnabled;
    }

    public void setCacheControlEnabled(boolean cacheControlEnabled) {
        this.cacheControlEnabled = cacheControlEnabled;
    }

    public String getCacheControlValue() {
        return cacheControlValue;
    }

    public void setCacheControlValue(String cacheControlValue) {
        this.cacheControlValue = cacheControlValue;
    }

    @Override
    public void configure(final String name, final Collection<String> healthCheckUrlPaths,
                          final HealthResponseProvider healthResponseProvider, final JerseyEnvironment jersey,
                          final ServletEnvironment servlets, final ObjectMapper mapper) {
        final JerseyHealthResponder jerseyHealthResponder = new JerseyHealthResponder(healthResponseProvider,
            cacheControlEnabled, cacheControlValue);

        final Resource.Builder resourceBuilder = Resource.builder();
        resourceBuilder.addMethod("GET")
            .produces(healthResponseProvider.contentType())
            .handledBy(jerseyHealthResponder)
            .build();

        final String concatenatedPaths = String.join(PATH_REGEX_DELIMITER, healthCheckUrlPaths);
        final String formattedPaths = String.format(PATH_TEMPLATE, concatenatedPaths);

        jersey.getResourceConfig().registerResources(resourceBuilder
            .path(formattedPaths)
            .build());
    }
}
