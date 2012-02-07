package com.yammer.dropwizard.db;

import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.validation.ValidationMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
public class DatabaseConfiguration {
    @NotNull
    @JsonProperty
    private String driverClass = null;

    @NotNull
    @JsonProperty
    private String user = null;

    @JsonProperty
    private String password = "";

    @NotNull
    @JsonProperty
    private String url = null;

    @NotNull
    @JsonProperty
    private ImmutableMap<String, String> properties = ImmutableMap.of();

    @NotNull
    @JsonProperty
    private Duration maxWaitForConnection = Duration.seconds(1);

    @NotNull
    @JsonProperty
    private String validationQuery = "/* Health Check */ SELECT 1";

    @Min(1)
    @Max(1024)
    @JsonProperty
    private int minSize = 1;

    @Min(1)
    @Max(1024)
    @JsonProperty
    private int maxSize = 8;

    @JsonProperty
    private boolean checkConnectionWhileIdle;

    @NotNull
    @JsonProperty
    private Duration checkConnectionHealthWhenIdleFor = Duration.seconds(10);

    @NotNull
    @JsonProperty
    private Duration closeConnectionIfIdleFor = Duration.minutes(1);

    public String getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ImmutableMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = ImmutableMap.copyOf(properties);
    }

    public Duration getMaxWaitForConnection() {
        return maxWaitForConnection;
    }

    public void setMaxWaitForConnection(Duration maxWaitForConnection) {
        this.maxWaitForConnection = maxWaitForConnection;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isCheckConnectionWhileIdle() {
        return checkConnectionWhileIdle;
    }

    public void setCheckConnectionWhileIdle(boolean checkConnectionWhileIdle) {
        this.checkConnectionWhileIdle = checkConnectionWhileIdle;
    }

    public Duration getCheckConnectionHealthWhenIdleFor() {
        return checkConnectionHealthWhenIdleFor;
    }

    public void setCheckConnectionHealthWhenIdleFor(Duration checkConnectionHealthWhenIdleFor) {
        this.checkConnectionHealthWhenIdleFor = checkConnectionHealthWhenIdleFor;
    }

    public Duration getCloseConnectionIfIdleFor() {
        return closeConnectionIfIdleFor;
    }

    public void setCloseConnectionIfIdleFor(Duration closeConnectionIfIdleFor) {
        this.closeConnectionIfIdleFor = closeConnectionIfIdleFor;
    }

    @ValidationMethod(message = ".minSize must be less than or equal to maxSize")
    public boolean isPoolSizedCorrectly() {
        return minSize <= maxSize;
    }
}
