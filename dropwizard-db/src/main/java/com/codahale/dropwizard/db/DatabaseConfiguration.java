package com.codahale.dropwizard.db;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("UnusedDeclaration")
public class DatabaseConfiguration {
    public enum TransactionIsolation {
        NONE(Connection.TRANSACTION_NONE),
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
        DEFAULT(DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION);

        private final int value;

        TransactionIsolation(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        @Override
        @JsonValue
        public String toString() {
            return super.toString().replace("_", "+").toLowerCase(Locale.ENGLISH);
        }

        @JsonCreator
        public static TransactionIsolation parse(String type) {
            return valueOf(type.toUpperCase(Locale.ENGLISH).replace("[^A-Za-z]", "_"));
        }
    }

    @NotNull
    @JsonProperty
    private String driverClass = null;

    @Min(0)
    @Max(100)
    @JsonProperty
    private int abandonWhenPercentageFull = 0;

    @JsonProperty
    private boolean alternateUsernameAllowed = false;

    @JsonProperty
    private boolean commitOnReturn = false;

    @JsonProperty
    private Boolean autoCommitByDefault;

    @JsonProperty
    private Boolean readOnlyByDefault;

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

    @JsonProperty
    private String defaultCatalog;

    @NotNull
    @JsonProperty
    private TransactionIsolation defaultTransactionIsolation = TransactionIsolation.DEFAULT;

    @JsonProperty
    private boolean useFairQueue = true;

    @Min(1)
    @JsonProperty
    private int initialSize = 10;

    @Min(1)
    @JsonProperty
    private int minSize = 10;

    @Min(1)
    @JsonProperty
    private int maxSize = 100;

    @JsonProperty
    private String initializationQuery;

    @JsonProperty
    private boolean logAbandonedQueries = false;

    @JsonProperty
    private boolean logValidationErrors = false;

    @JsonProperty
    @MinDuration(1)
    private Duration maxConnectionAge;

    @NotNull
    @JsonProperty
    @MinDuration(1)
    private Duration maxWaitForConnection = Duration.seconds(30);

    @NotNull
    @JsonProperty
    @MinDuration(1)
    private Duration minIdleTime = Duration.minutes(1);

    @NotNull
    @JsonProperty
    private String validationQuery = "/* Health Check */ SELECT 1";

    @JsonProperty
    private boolean checkConnectionWhileIdle = true;

    @JsonProperty
    private boolean checkConnectionOnBorrow = false;

    @JsonProperty
    private boolean checkConnectionOnConnect = false;

    @JsonProperty
    private boolean checkConnectionOnReturn = false;

    @JsonProperty
    private boolean defaultReadOnly = false;

    @JsonProperty
    private boolean autoCommentsEnabled = true;

    @NotNull
    @JsonProperty
    @MinDuration(1)
    private Duration evictionInterval = Duration.seconds(5);

    @NotNull
    @JsonProperty
    @MinDuration(1)
    private Duration validationInterval = Duration.seconds(30);

    public boolean isAutoCommentsEnabled() {
        return autoCommentsEnabled;
    }

    public void setAutoCommentsEnabled(boolean autoCommentsEnabled) {
        this.autoCommentsEnabled = autoCommentsEnabled;
    }

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

    public boolean getCheckConnectionWhileIdle() {
        return checkConnectionWhileIdle;
    }

    public void setCheckConnectionWhileIdle(boolean checkConnectionWhileIdle) {
        this.checkConnectionWhileIdle = checkConnectionWhileIdle;
    }

    public boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }

    public void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    @ValidationMethod(message = ".minSize must be less than or equal to maxSize")
    public boolean isMinSizeLessThanMaxSize() {
        return minSize <= maxSize;
    }

    @ValidationMethod(message = ".initialSize must be less than or equal to maxSize")
    public boolean isInitialSizeLessThanMaxSize() {
        return initialSize <= maxSize;
    }

    @ValidationMethod(message = ".initialSize must be greater than or equal to minSize")
    public boolean isInitialSizeGreaterThanMinSize() {
        return minSize <= initialSize;
    }

    public int getAbandonWhenPercentageFull() {
        return abandonWhenPercentageFull;
    }

    public void setAbandonWhenPercentageFull(int percentage) {
        this.abandonWhenPercentageFull = percentage;
    }

    public boolean isAlternateUsernameAllowed() {
        return alternateUsernameAllowed;
    }

    public void setAlternateUsernameAllowed(boolean allow) {
        this.alternateUsernameAllowed = allow;
    }

    public boolean getCommitOnReturn() {
        return commitOnReturn;
    }

    public void setCommitOnReturn(boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
    }

    public Boolean getAutoCommitByDefault() {
        return autoCommitByDefault;
    }

    public void setAutoCommitByDefault(Boolean autoCommit) {
        this.autoCommitByDefault = autoCommit;
    }

    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    public Boolean getReadOnlyByDefault() {
        return readOnlyByDefault;
    }

    public void setReadOnlyByDefault(Boolean readOnlyByDefault) {
        this.readOnlyByDefault = readOnlyByDefault;
    }

    public TransactionIsolation getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    public void setDefaultTransactionIsolation(TransactionIsolation defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    public boolean getUseFairQueue() {
        return useFairQueue;
    }

    public void setUseFairQueue(boolean fair) {
        this.useFairQueue = fair;
    }

    public int getInitialSize() {
        return initialSize;
    }

    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    public String getInitializationQuery() {
        return initializationQuery;
    }

    public void setInitializationQuery(String query) {
        this.initializationQuery = query;
    }

    public boolean getLogAbandonedQueries() {
        return logAbandonedQueries;
    }

    public void setLogAbandonedQueries(boolean log) {
        this.logAbandonedQueries = log;
    }

    public boolean getLogValidationErrors() {
        return logValidationErrors;
    }

    public void setLogValidationErrors(boolean log) {
        this.logValidationErrors = log;
    }

    public Optional<Duration> getMaxConnectionAge() {
        return Optional.fromNullable(maxConnectionAge);
    }

    public void setMaxConnectionAge(Duration age) {
        this.maxConnectionAge = age;
    }

    public Duration getMinIdleTime() {
        return minIdleTime;
    }

    public void setMinIdleTime(Duration time) {
        this.minIdleTime = time;
    }

    public boolean getCheckConnectionOnBorrow() {
        return checkConnectionOnBorrow;
    }

    public void setCheckConnectionOnBorrow(boolean checkConnectionOnBorrow) {
        this.checkConnectionOnBorrow = checkConnectionOnBorrow;
    }

    public boolean getCheckConnectionOnConnect() {
        return checkConnectionOnConnect;
    }

    public void setCheckConnectionOnConnect(boolean checkConnectionOnConnect) {
        this.checkConnectionOnConnect = checkConnectionOnConnect;
    }

    public boolean getCheckConnectionOnReturn() {
        return checkConnectionOnReturn;
    }

    public void setCheckConnectionOnReturn(boolean checkConnectionOnReturn) {
        this.checkConnectionOnReturn = checkConnectionOnReturn;
    }

    public Duration getEvictionInterval() {
        return evictionInterval;
    }

    public void setEvictionInterval(Duration interval) {
        this.evictionInterval = interval;
    }

    public Duration getValidationInterval() {
        return validationInterval;
    }

    public void setValidationInterval(Duration validationInterval) {
        this.validationInterval = validationInterval;
    }
}
