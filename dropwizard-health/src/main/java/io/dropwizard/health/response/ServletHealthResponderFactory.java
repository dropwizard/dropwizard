package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthEnvironment;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.setup.ServletEnvironment;

import java.util.Collection;

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
                          final HealthResponseProvider healthResponseProvider,
                          final HealthEnvironment health, final JerseyEnvironment jersey,
                          final ServletEnvironment servlets, final ObjectMapper mapper) {
        final ServletHealthResponder servlet = new ServletHealthResponder(healthResponseProvider, cacheControlEnabled,
            cacheControlValue);
        servlets
            .addServlet(name + SERVLET_SUFFIX, servlet)
            .addMapping(healthCheckUrlPaths.toArray(new String[0]));
    }
}
