package com.yammer.dropwizard.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.util.Duration;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
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

    @JsonProperty
    private boolean defaultReadOnly = false;

    @JsonProperty
    private ImmutableList<String> connectionInitializationStatements = ImmutableList.of();

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

    public boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    public ImmutableList<String> getConnectionInitializationStatements() {
        return connectionInitializationStatements;
    }

    public void setConnectionInitializationStatements(List<String> statements) {
        this.connectionInitializationStatements = ImmutableList.copyOf(statements);
    }

    @ValidationMethod(message = ".minSize must be less than or equal to maxSize")
    public boolean isPoolSizedCorrectly() {
        return minSize <= maxSize;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if ((obj == null) || (getClass() != obj.getClass())) { return false; }
        final DatabaseConfiguration that = (DatabaseConfiguration) obj;
        return (checkConnectionWhileIdle == that.checkConnectionWhileIdle) &&
                (maxSize == that.maxSize) &&
                (minSize == that.minSize) &&
                !((checkConnectionHealthWhenIdleFor != null) ? !checkConnectionHealthWhenIdleFor.equals(that.checkConnectionHealthWhenIdleFor) : (that.checkConnectionHealthWhenIdleFor != null)) &&
                !((closeConnectionIfIdleFor != null) ? !closeConnectionIfIdleFor.equals(that.closeConnectionIfIdleFor) : (that.closeConnectionIfIdleFor != null)) &&
                (defaultReadOnly == that.defaultReadOnly) &&
                !((driverClass != null) ? !driverClass.equals(that.driverClass) : (that.driverClass != null)) &&
                !((maxWaitForConnection != null) ? !maxWaitForConnection.equals(that.maxWaitForConnection) : (that.maxWaitForConnection != null)) &&
                !((password != null) ? !password.equals(that.password) : (that.password != null)) &&
                !((properties != null) ? !properties.equals(that.properties) : (that.properties != null)) &&
                !((url != null) ? !url.equals(that.url) : (that.url != null)) &&
                !((user != null) ? !user.equals(that.user) : (that.user != null)) &&
                !((validationQuery != null) ? !validationQuery.equals(that.validationQuery) : (that.validationQuery != null)) &&
                !((connectionInitializationStatements != null) ? !connectionInitializationStatements.equals(that.connectionInitializationStatements) : (that.connectionInitializationStatements != null));
    }

    @Override
    public int hashCode() {
        int result = (driverClass != null) ? driverClass.hashCode() : 0;
        result = (31 * result) + ((user != null) ? user.hashCode() : 0);
        result = (31 * result) + ((password != null) ? password.hashCode() : 0);
        result = (31 * result) + ((url != null) ? url.hashCode() : 0);
        result = (31 * result) + ((properties != null) ? properties.hashCode() : 0);
        result = (31 * result) + ((maxWaitForConnection != null) ? maxWaitForConnection.hashCode() : 0);
        result = (31 * result) + ((validationQuery != null) ? validationQuery.hashCode() : 0);
        result = (31 * result) + minSize;
        result = (31 * result) + maxSize;
        result = (31 * result) + (checkConnectionWhileIdle ? 1 : 0);
        result = (31 * result) + ((checkConnectionHealthWhenIdleFor != null) ? checkConnectionHealthWhenIdleFor.hashCode() : 0);
        result = (31 * result) + ((closeConnectionIfIdleFor != null) ? closeConnectionIfIdleFor.hashCode() : 0);
        result = (31 * result) + (defaultReadOnly ? 1 : 0);
        result = (31 * result) + ((connectionInitializationStatements != null) ? connectionInitializationStatements.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                      .add("driverClass", driverClass)
                      .add("user", user)
                      .add("password", password)
                      .add("url", url)
                      .add("properties", properties)
                      .add("maxWaitForConnection", maxWaitForConnection)
                      .add("validationQuery", validationQuery)
                      .add("minSize", minSize)
                      .add("maxSize", maxSize)
                      .add("checkConnectionWhileIdle", checkConnectionWhileIdle)
                      .add("checkConnectionHealthWhenIdleFor", checkConnectionHealthWhenIdleFor)
                      .add("closeConnectionIfIdleFor", closeConnectionIfIdleFor)
                      .add("defaultReadOnly", defaultReadOnly)
                      .add("connectionInitializationStatements", connectionInitializationStatements)
                      .toString();
    }
}
