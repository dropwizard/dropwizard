package com.codahale.dropwizard.db;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

// TODO: 5/15/13 <coda> -- add tests for DataSourceFactory
// TODO: 5/15/13 <coda> -- add docs for DataSourceFactory

@SuppressWarnings("UnusedDeclaration")
public class DataSourceFactory {
    public enum TransactionIsolation {
        NONE(Connection.TRANSACTION_NONE),
        READ_UNCOMMITTED(Connection.TRANSACTION_READ_UNCOMMITTED),
        READ_COMMITTED(Connection.TRANSACTION_READ_COMMITTED),
        REPEATABLE_READ(Connection.TRANSACTION_REPEATABLE_READ),
        SERIALIZABLE(Connection.TRANSACTION_SERIALIZABLE),
        DEFAULT(org.apache.tomcat.jdbc.pool.DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION);

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
    private String driverClass = null;

    @Min(0)
    @Max(100)
    private int abandonWhenPercentageFull = 0;

    private boolean alternateUsernamesAllowed = false;

    private boolean commitOnReturn = false;

    private Boolean autoCommitByDefault;

    private Boolean readOnlyByDefault;

    @NotNull
    private String user = null;

    private String password = "";

    @NotNull
    private String url = null;

    @NotNull
    private Map<String, String> properties = Maps.newLinkedHashMap();

    private String defaultCatalog;

    @NotNull
    private TransactionIsolation defaultTransactionIsolation = TransactionIsolation.DEFAULT;

    private boolean useFairQueue = true;

    @Min(1)
    private int initialSize = 10;

    @Min(1)
    private int minSize = 10;

    @Min(1)
    private int maxSize = 100;

    private String initializationQuery;

    private boolean logAbandonedQueries = false;

    private boolean logValidationErrors = false;

    @MinDuration(1)
    private Duration maxConnectionAge;

    @NotNull
    @MinDuration(1)
    private Duration maxWaitForConnection = Duration.seconds(30);

    @NotNull
    @MinDuration(1)
    private Duration minIdleTime = Duration.minutes(1);

    @NotNull
    private String validationQuery = "/* Health Check */ SELECT 1";

    private boolean checkConnectionWhileIdle = true;

    private boolean checkConnectionOnBorrow = false;

    private boolean checkConnectionOnConnect = false;

    private boolean checkConnectionOnReturn = false;

    private boolean defaultReadOnly = false;

    private boolean autoCommentsEnabled = true;

    @NotNull
    @MinDuration(1)
    private Duration evictionInterval = Duration.seconds(5);

    @NotNull
    @MinDuration(1)
    private Duration validationInterval = Duration.seconds(30);

    @JsonProperty
    public boolean isAutoCommentsEnabled() {
        return autoCommentsEnabled;
    }

    @JsonProperty
    public void setAutoCommentsEnabled(boolean autoCommentsEnabled) {
        this.autoCommentsEnabled = autoCommentsEnabled;
    }

    @JsonProperty
    public String getDriverClass() {
        return driverClass;
    }

    @JsonProperty
    public void setDriverClass(String driverClass) {
        this.driverClass = driverClass;
    }

    @JsonProperty
    public String getUser() {
        return user;
    }

    @JsonProperty
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public String getUrl() {
        return url;
    }

    @JsonProperty
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty
    public Map<String, String> getProperties() {
        return properties;
    }

    @JsonProperty
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @JsonProperty
    public Duration getMaxWaitForConnection() {
        return maxWaitForConnection;
    }

    @JsonProperty
    public void setMaxWaitForConnection(Duration maxWaitForConnection) {
        this.maxWaitForConnection = maxWaitForConnection;
    }

    @JsonProperty
    public String getValidationQuery() {
        return validationQuery;
    }

    @JsonProperty
    public void setValidationQuery(String validationQuery) {
        this.validationQuery = validationQuery;
    }

    @JsonProperty
    public int getMinSize() {
        return minSize;
    }

    @JsonProperty
    public void setMinSize(int minSize) {
        this.minSize = minSize;
    }

    @JsonProperty
    public int getMaxSize() {
        return maxSize;
    }

    @JsonProperty
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @JsonProperty
    public boolean getCheckConnectionWhileIdle() {
        return checkConnectionWhileIdle;
    }

    @JsonProperty
    public void setCheckConnectionWhileIdle(boolean checkConnectionWhileIdle) {
        this.checkConnectionWhileIdle = checkConnectionWhileIdle;
    }

    @JsonProperty
    public boolean isDefaultReadOnly() {
        return defaultReadOnly;
    }

    @JsonProperty
    public void setDefaultReadOnly(boolean defaultReadOnly) {
        this.defaultReadOnly = defaultReadOnly;
    }

    @JsonIgnore
    @ValidationMethod(message = ".minSize must be less than or equal to maxSize")
    public boolean isMinSizeLessThanMaxSize() {
        return minSize <= maxSize;
    }

    @JsonIgnore
    @ValidationMethod(message = ".initialSize must be less than or equal to maxSize")
    public boolean isInitialSizeLessThanMaxSize() {
        return initialSize <= maxSize;
    }

    @JsonIgnore
    @ValidationMethod(message = ".initialSize must be greater than or equal to minSize")
    public boolean isInitialSizeGreaterThanMinSize() {
        return minSize <= initialSize;
    }

    @JsonProperty
    public int getAbandonWhenPercentageFull() {
        return abandonWhenPercentageFull;
    }

    @JsonProperty
    public void setAbandonWhenPercentageFull(int percentage) {
        this.abandonWhenPercentageFull = percentage;
    }

    @JsonProperty
    public boolean isAlternateUsernamesAllowed() {
        return alternateUsernamesAllowed;
    }

    @JsonProperty
    public void setAlternateUsernamesAllowed(boolean allow) {
        this.alternateUsernamesAllowed = allow;
    }

    @JsonProperty
    public boolean getCommitOnReturn() {
        return commitOnReturn;
    }

    @JsonProperty
    public void setCommitOnReturn(boolean commitOnReturn) {
        this.commitOnReturn = commitOnReturn;
    }

    @JsonProperty
    public Boolean getAutoCommitByDefault() {
        return autoCommitByDefault;
    }

    @JsonProperty
    public void setAutoCommitByDefault(Boolean autoCommit) {
        this.autoCommitByDefault = autoCommit;
    }

    @JsonProperty
    public String getDefaultCatalog() {
        return defaultCatalog;
    }

    @JsonProperty
    public void setDefaultCatalog(String defaultCatalog) {
        this.defaultCatalog = defaultCatalog;
    }

    @JsonProperty
    public Boolean getReadOnlyByDefault() {
        return readOnlyByDefault;
    }

    @JsonProperty
    public void setReadOnlyByDefault(Boolean readOnlyByDefault) {
        this.readOnlyByDefault = readOnlyByDefault;
    }

    @JsonProperty
    public TransactionIsolation getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    @JsonProperty
    public void setDefaultTransactionIsolation(TransactionIsolation defaultTransactionIsolation) {
        this.defaultTransactionIsolation = defaultTransactionIsolation;
    }

    @JsonProperty
    public boolean getUseFairQueue() {
        return useFairQueue;
    }

    @JsonProperty
    public void setUseFairQueue(boolean fair) {
        this.useFairQueue = fair;
    }

    @JsonProperty
    public int getInitialSize() {
        return initialSize;
    }

    @JsonProperty
    public void setInitialSize(int initialSize) {
        this.initialSize = initialSize;
    }

    @JsonProperty
    public String getInitializationQuery() {
        return initializationQuery;
    }

    @JsonProperty
    public void setInitializationQuery(String query) {
        this.initializationQuery = query;
    }

    @JsonProperty
    public boolean getLogAbandonedQueries() {
        return logAbandonedQueries;
    }

    @JsonProperty
    public void setLogAbandonedQueries(boolean log) {
        this.logAbandonedQueries = log;
    }

    @JsonProperty
    public boolean getLogValidationErrors() {
        return logValidationErrors;
    }

    @JsonProperty
    public void setLogValidationErrors(boolean log) {
        this.logValidationErrors = log;
    }

    @JsonProperty
    public Optional<Duration> getMaxConnectionAge() {
        return Optional.fromNullable(maxConnectionAge);
    }

    @JsonProperty
    public void setMaxConnectionAge(Duration age) {
        this.maxConnectionAge = age;
    }

    @JsonProperty
    public Duration getMinIdleTime() {
        return minIdleTime;
    }

    @JsonProperty
    public void setMinIdleTime(Duration time) {
        this.minIdleTime = time;
    }

    @JsonProperty
    public boolean getCheckConnectionOnBorrow() {
        return checkConnectionOnBorrow;
    }

    @JsonProperty
    public void setCheckConnectionOnBorrow(boolean checkConnectionOnBorrow) {
        this.checkConnectionOnBorrow = checkConnectionOnBorrow;
    }

    @JsonProperty
    public boolean getCheckConnectionOnConnect() {
        return checkConnectionOnConnect;
    }

    @JsonProperty
    public void setCheckConnectionOnConnect(boolean checkConnectionOnConnect) {
        this.checkConnectionOnConnect = checkConnectionOnConnect;
    }

    @JsonProperty
    public boolean getCheckConnectionOnReturn() {
        return checkConnectionOnReturn;
    }

    @JsonProperty
    public void setCheckConnectionOnReturn(boolean checkConnectionOnReturn) {
        this.checkConnectionOnReturn = checkConnectionOnReturn;
    }

    @JsonProperty
    public Duration getEvictionInterval() {
        return evictionInterval;
    }

    @JsonProperty
    public void setEvictionInterval(Duration interval) {
        this.evictionInterval = interval;
    }

    @JsonProperty
    public Duration getValidationInterval() {
        return validationInterval;
    }

    @JsonProperty
    public void setValidationInterval(Duration validationInterval) {
        this.validationInterval = validationInterval;
    }

    public ManagedDataSource build(MetricRegistry metricRegistry,
                                   String name) throws ClassNotFoundException {
        final Properties properties = new Properties();
        for (Map.Entry<String, String> property : this.properties.entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }

        final PoolProperties poolConfig = new PoolProperties();
        poolConfig.setAbandonWhenPercentageFull(abandonWhenPercentageFull);
        poolConfig.setAlternateUsernameAllowed(alternateUsernamesAllowed);
        poolConfig.setCommitOnReturn(commitOnReturn);
        poolConfig.setDbProperties(properties);
        poolConfig.setDefaultAutoCommit(autoCommitByDefault);
        poolConfig.setDefaultCatalog(defaultCatalog);
        poolConfig.setDefaultReadOnly(readOnlyByDefault);
        poolConfig.setDefaultTransactionIsolation(defaultTransactionIsolation.getValue());
        poolConfig.setDriverClassName(driverClass);
        poolConfig.setFairQueue(useFairQueue);
        poolConfig.setInitialSize(initialSize);
        poolConfig.setInitSQL(initializationQuery);
        poolConfig.setLogAbandoned(logAbandonedQueries);
        poolConfig.setLogValidationErrors(logValidationErrors);
        poolConfig.setMaxActive(maxSize);
        poolConfig.setMaxIdle(maxSize);
        poolConfig.setMinIdle(minSize);

        if (maxConnectionAge != null) {
            poolConfig.setMaxAge(maxConnectionAge.toMilliseconds());
        }

        poolConfig.setMaxWait((int) maxWaitForConnection.toMilliseconds());
        poolConfig.setMinEvictableIdleTimeMillis((int) minIdleTime.toMilliseconds());
        poolConfig.setName(name);
        poolConfig.setUrl(url);
        poolConfig.setUsername(user);
        poolConfig.setPassword(password);
        poolConfig.setTestWhileIdle(checkConnectionWhileIdle);
        poolConfig.setValidationQuery(validationQuery);
        poolConfig.setTestOnBorrow(checkConnectionOnBorrow);
        poolConfig.setTestOnConnect(checkConnectionOnConnect);
        poolConfig.setTestOnReturn(checkConnectionOnReturn);
        poolConfig.setTimeBetweenEvictionRunsMillis((int) evictionInterval.toMilliseconds());
        poolConfig.setValidationInterval(1);

        return new ManagedPooledDataSource(poolConfig, metricRegistry);
    }
}
