package io.dropwizard.health.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.health.DetailedJsonHealthServlet;
import io.dropwizard.health.HealthStateAggregator;
import io.dropwizard.health.HealthStatusChecker;

import javax.servlet.http.HttpServlet;

@JsonTypeName("detailed-json")
public class DetailedJsonHealthServletFactory implements HealthServletFactory {
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
    public HttpServlet build(final HealthStatusChecker healthStatusChecker,
                             final HealthStateAggregator healthStateAggregator, final ObjectMapper mapper) {
        return new DetailedJsonHealthServlet(healthStatusChecker, healthStateAggregator, mapper, cacheControlEnabled,
                cacheControlValue);
    }
}
