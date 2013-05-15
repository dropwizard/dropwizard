package com.codahale.dropwizard.db;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.OneOf;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Connection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * A factory for pooled {@link ManagedDataSource}s.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code driverClass}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The full name of the JDBC driver class.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code url}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The URL of the server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code user}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The username used to connect to the server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code password}</td>
 *         <td>none</td>
 *         <td>The password used to connect to the server.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code abandonWhenPercentageFull}</td>
 *         <td>0</td>
 *         <td>
 *             Connections that have been abandoned (timed out) won't get closed and reported up
 *             unless the number of connections in use are above the percentage defined by
 *             {@code abandonWhenPercentageFull}. The value should be between 0-100.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code alternateUsernamesAllowed}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             Set to true if the call
 *             {@link javax.sql.DataSource#getConnection(String, String) getConnection(username,password)}
 *             is allowed. This is used for when the pool is used by an application accessing
 *             multiple schemas. There is a performance impact turning this option on, even when not
 *             used.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code commitOnReturn}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             Set to true if you want the connection pool to commit any pending transaction when a
 *             connection is returned.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code autoCommitByDefault}</td>
 *         <td>JDBC driver's default</td>
 *         <td>The default auto-commit state of the connections.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code readOnlyByDefault}</td>
 *         <td>JDBC driver's default</td>
 *         <td>The default read-only state of the connections.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code properties}</td>
 *         <td>none</td>
 *         <td>Any additional JDBC driver parameters.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code defaultCatalog}</td>
 *         <td>none</td>
 *         <td>The default catalog to use for the connections.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code defaultTransactionIsolation}</td>
 *         <td>JDBC driver default</td>
 *         <td>
 *             The default transaction isolation to use for the connections. Can be one of
 *             {@code none}, {@code default}, {@code read-uncommitted}, {@code read-committed},
 *             {@code repeatable-read}, or {@code serializable}.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code useFairQueue}</td>
 *         <td>{@code true}</td>
 *         <td>
 *             If {@code true}, calls to {@code getConnection} are handled in a FIFO manner.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code initialSize}</td>
 *         <td>10</td>
 *         <td>
 *             The initial size of the connection pool.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code minSize}</td>
 *         <td>10</td>
 *         <td>
 *             The minimum size of the connection pool.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxSize}</td>
 *         <td>10</td>
 *         <td>
 *             The maximum size of the connection pool.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code initializationQuery}</td>
 *         <td>none</td>
 *         <td>
 *             A custom query to be run when a connection is first created.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code logAbandonedConnections}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             If {@code true}, logs stack traces of abandoned connections.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code logValidationErrors}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             If {@code true}, logs errors when connections fail validation.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxConnectionAge}</td>
 *         <td>none</td>
 *         <td>
 *             If set, connections which have been open for longer than {@code maxConnectionAge} are
 *             closed when returned.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxWaitForConnection}</td>
 *         <td>30 seconds</td>
 *         <td>
 *             If a request for a connection is blocked for longer than this period, an exception
 *             will be thrown.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code minIdleTime}</td>
 *         <td>1 minute</td>
 *         <td>
 *             The minimum amount of time an connection must sit idle in the pool before it is
 *             eligible for eviction.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code validationQuery}</td>
 *         <td>{@code SELECT 1}</td>
 *         <td>
 *             The SQL query that will be used to validate connections from this pool before
 *             returning them to the caller or pool. If specified, this query does not have to
 *             return any data, it just can't throw a SQLException.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code checkConnectionWhileIdle}</td>
 *         <td>{@code true}</td>
 *         <td>
 *             Set to true if query validation should take place while the connection is idle.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code checkConnectionOnBorrow}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             Whether or not connections will be validated before being borrowed from the pool. If
 *             the connection fails to validate, it will be dropped from the pool, and another will
 *             be borrowed.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code checkConnectionOnConnect}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             Whether or not connections will be validated before being added to the pool. If the
 *             connection fails to validate, it won't be added to the pool.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code checkConnectionOnReturn}</td>
 *         <td>{@code false}</td>
 *         <td>
 *             Whether or not connections will be validated after being returned to the pool. If
 *             the connection fails to validate, it will be dropped from the pool.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code autoCommentsEnabled}</td>
 *         <td>{@code true}</td>
 *         <td>
 *             Whether or not ORMs should automatically add comments.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code evictionInterval}</td>
 *         <td>5 seconds</td>
 *         <td>
 *             The amount of time to sleep between runs of the idle connection validation, abandoned
 *             cleaner and idle pool resizing.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code validationInterval}</td>
 *         <td>30 seconds</td>
 *         <td>
 *             To avoid excess validation, only run validation once every interval.
 *         </td>
 *     </tr>
 * </table>
 */
public class DataSourceFactory {
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
    @OneOf(
            value = {"none", "default", "read-uncommitted", "read-committed", "repeatable-read", "serializable"},
            ignoreCase = true,
            ignoreWhitespace = true
    )
    private String defaultTransactionIsolation = "default";

    private boolean useFairQueue = true;

    @Min(1)
    private int initialSize = 10;

    @Min(1)
    private int minSize = 10;

    @Min(1)
    private int maxSize = 100;

    private String initializationQuery;

    private boolean logAbandonedConnections = false;

    private boolean logValidationErrors = false;

    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration maxConnectionAge;

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration maxWaitForConnection = Duration.seconds(30);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.SECONDS)
    private Duration minIdleTime = Duration.minutes(1);

    @NotNull
    private String validationQuery = "/* Health Check */ SELECT 1";

    private boolean checkConnectionWhileIdle = true;

    private boolean checkConnectionOnBorrow = false;

    private boolean checkConnectionOnConnect = true;

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
    public String getDefaultTransactionIsolation() {
        return defaultTransactionIsolation;
    }

    @JsonProperty
    public void setDefaultTransactionIsolation(String isolation) {
        this.defaultTransactionIsolation = isolation;
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
    public boolean getLogAbandonedConnections() {
        return logAbandonedConnections;
    }

    @JsonProperty
    public void setLogAbandonedConnections(boolean log) {
        this.logAbandonedConnections = log;
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
        poolConfig.setDefaultTransactionIsolation(getIsolation());
        poolConfig.setDriverClassName(driverClass);
        poolConfig.setFairQueue(useFairQueue);
        poolConfig.setInitialSize(initialSize);
        poolConfig.setInitSQL(initializationQuery);
        poolConfig.setLogAbandoned(logAbandonedConnections);
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
        poolConfig.setValidationInterval(validationInterval.toMilliseconds());

        return new ManagedPooledDataSource(poolConfig, metricRegistry);
    }

    private int getIsolation() {
        switch (defaultTransactionIsolation.toLowerCase()) {
            case "none":
                return Connection.TRANSACTION_NONE;
            case "read-uncommitted":
                return Connection.TRANSACTION_READ_UNCOMMITTED;
            case "read-committed":
                return Connection.TRANSACTION_READ_COMMITTED;
            case "repeatable-read":
                return Connection.TRANSACTION_REPEATABLE_READ;
            case "serializable":
                return Connection.TRANSACTION_SERIALIZABLE;
            default:
                return org.apache.tomcat.jdbc.pool.DataSourceFactory.UNKNOWN_TRANSACTIONISOLATION;
        }
    }
}
