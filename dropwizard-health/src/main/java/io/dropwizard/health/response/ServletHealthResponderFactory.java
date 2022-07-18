package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthEnvironment;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static java.util.Collections.singletonList;

/**
 * A servlet-based implementation of {@link HealthResponderFactory}, to respond to health check requests.
 */
@JsonTypeName("servlet")
public class ServletHealthResponderFactory implements HealthResponderFactory {
    static final String SERVLET_SUFFIX = "-servlet";

    @JsonProperty
    private boolean cacheControlEnabled = true;

    @JsonProperty
    private String cacheControlValue = "no-store";

    @NotNull
    @JsonProperty
    private List<String> livenessCheckUrlPaths = Collections.emptyList();

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

    public List<String> getLivenessCheckUrlPaths() {
        return livenessCheckUrlPaths;
    }

    public void setLivenessCheckUrlPaths(List<String> livenessCheckUrlPaths) {
        this.livenessCheckUrlPaths = livenessCheckUrlPaths;
    }

    @Override
    public void configure(final String name, final Collection<String> healthCheckUrlPaths,
                          final HealthResponseProvider healthResponseProvider,
                          final HealthEnvironment health, final JerseyEnvironment jersey,
                          final ServletEnvironment servlets, final ObjectMapper mapper) {
        final ServletHealthResponder servlet = new ServletHealthResponder(healthResponseProvider, cacheControlEnabled,
            cacheControlValue);

        String[] combinedMapping = Stream.of(healthCheckUrlPaths, livenessCheckUrlPaths).flatMap(Collection::stream).toArray(String[]::new);
        servlets
            .addServlet(name + SERVLET_SUFFIX, servlet)
            .addMapping(combinedMapping);
    }
}
