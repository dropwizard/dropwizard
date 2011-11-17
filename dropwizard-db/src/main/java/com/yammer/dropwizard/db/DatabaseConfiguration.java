package com.yammer.dropwizard.db;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.yammer.dropwizard.util.Duration;
import org.hibernate.validator.constraints.URL;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

@SuppressWarnings("FieldMayBeFinal")
public class DatabaseConfiguration {
    public static class DatabaseConnectionConfiguration {
        @NotNull
        private String driverClass = null;
        
        @NotNull
        private String user = null;

        private String password = "";

        @NotNull
        private String url = null;

        @NotNull
        private Map<String, String> properties = Maps.newHashMap();

        @NotNull
        @Pattern(regexp = Duration.VALID_DURATION)
        private String maxWaitForConnection = "8ms";
        
        @NotNull
        private String validationQuery = "/* Health Check */ SELECT 1";

        // TODO: 11/16/11 <coda> -- validate minSize <= maxSize

        @Max(1024)
        @Min(1)
        private int minSize = 1;

        @Max(1024)
        @Min(1)
        private int maxSize = 1;

        private boolean checkConnectionWhileIdle;

        @NotNull
        @Pattern(regexp = Duration.VALID_DURATION)
        private String checkConnectionHealthWhenIdleFor = "10s";

        @NotNull
        @Pattern(regexp = Duration.VALID_DURATION)
        private String closeConnectionIfIdleFor = "10m";

        public String getDriverClass() {
            return driverClass;
        }

        public void setDriverClass(String driverClass) {
            this.driverClass = driverClass;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = ImmutableMap.copyOf(properties);
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public Duration getMaxWaitForConnection() {
            return Duration.parse(maxWaitForConnection);
        }

        public void setMaxWaitForConnection(Duration maxWait) {
            this.maxWaitForConnection = maxWait.toString();
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

        public boolean checkConnectionWhileIdle() {
            return checkConnectionWhileIdle;
        }

        public void setCheckConnectionWhileIdle(boolean checkConnectionWhileIdle) {
            this.checkConnectionWhileIdle = checkConnectionWhileIdle;
        }

        public Duration getCheckConnectionHealthWhenIdleFor() {
            return Duration.parse(checkConnectionHealthWhenIdleFor);
        }

        public void setCheckConnectionHealthWhenIdleFor(Duration timeout) {
            this.checkConnectionHealthWhenIdleFor = timeout.toString();
        }

        public Duration getCloseConnectionIfIdleFor() {
            return Duration.parse(closeConnectionIfIdleFor);
        }

        public void setCloseConnectionIfIdleFor(Duration timeout) {
            this.closeConnectionIfIdleFor = timeout.toString();
        }
    }

    @Valid
    private Map<String, DatabaseConnectionConfiguration> connections = Maps.newHashMap();
    
    public DatabaseConnectionConfiguration getConnection(String name) {
        return connections.get(name);
    }
    
    public void addConnection(String name, DatabaseConnectionConfiguration config) {
        connections.put(name, config);
    }
}
