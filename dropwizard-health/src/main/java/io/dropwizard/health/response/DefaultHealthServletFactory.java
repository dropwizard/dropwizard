package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.health.HealthCheckServlet;
import io.dropwizard.health.HealthStatusChecker;
import javax.servlet.http.HttpServlet;
import javax.ws.rs.core.MediaType;

/**
 * The default implementation of {@link HealthServletFactory}, which allows for configuration of the response.
 */
@JsonTypeName("default")
public class DefaultHealthServletFactory implements HealthServletFactory {
    private static final String STATUS_TEMPLATE = "{\"status\": \"%s\"}";

    @JsonProperty
    private boolean cacheControlEnabled = true;

    @JsonProperty
    private String cacheControlValue = "no-store";

    @JsonProperty
    private String contentType = MediaType.APPLICATION_JSON;

    @JsonProperty
    private String healthyValue = String.format(STATUS_TEMPLATE, "healthy");

    @JsonProperty
    private String unhealthyValue = String.format(STATUS_TEMPLATE, "unhealthy");

    public boolean isCacheControlEnabled() {
        return cacheControlEnabled;
    }

    public void setCacheControlEnabled(final boolean cacheControlEnabled) {
        this.cacheControlEnabled = cacheControlEnabled;
    }

    public String getCacheControlValue() {
        return cacheControlValue;
    }

    public void setCacheControlValue(final String cacheControlValue) {
        this.cacheControlValue = cacheControlValue;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(final String contentType) {
        this.contentType = contentType;
    }

    public String getHealthyValue() {
        return healthyValue;
    }

    public void setHealthyValue(final String healthyValue) {
        this.healthyValue = healthyValue;
    }

    public String getUnhealthyValue() {
        return unhealthyValue;
    }

    public void setUnhealthyValue(final String unhealthyValue) {
        this.unhealthyValue = unhealthyValue;
    }

    @Override
    public HttpServlet build(HealthStatusChecker healthStatusChecker) {
        return new HealthCheckServlet(healthStatusChecker, cacheControlEnabled, cacheControlValue, contentType,
                healthyValue, unhealthyValue);
    }
}
