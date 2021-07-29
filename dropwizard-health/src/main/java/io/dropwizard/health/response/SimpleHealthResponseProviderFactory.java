package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;

@JsonTypeName("simple")
public class SimpleHealthResponseProviderFactory implements HealthResponseProviderFactory {
    private static final String STATUS_TEMPLATE = "{\"status\": \"%s\"}";

    @NotNull
    @JsonProperty
    private String contentType = MediaType.APPLICATION_JSON;

    @NotNull
    @JsonProperty
    private String healthyValue = String.format(STATUS_TEMPLATE, "healthy");

    @NotNull
    @JsonProperty
    private String unhealthyValue = String.format(STATUS_TEMPLATE, "unhealthy");

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getHealthyValue() {
        return healthyValue;
    }

    public void setHealthyValue(String healthyValue) {
        this.healthyValue = healthyValue;
    }

    public String getUnhealthyValue() {
        return unhealthyValue;
    }

    public void setUnhealthyValue(String unhealthyValue) {
        this.unhealthyValue = unhealthyValue;
    }

    @Override
    public HealthResponseProvider build(final HealthStatusChecker healthStatusChecker,
                                        final HealthStateAggregator healthStateAggregator,
                                        final ObjectMapper mapper) {
        return new SimpleHealthResponseProvider(healthStatusChecker, contentType, healthyValue, unhealthyValue);
    }
}
