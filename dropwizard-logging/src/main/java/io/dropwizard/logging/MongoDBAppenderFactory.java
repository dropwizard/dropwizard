package io.dropwizard.logging;

import io.dropwizard.util.Size;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mongodb.ReadPreference;


/**
 * An {@link AppenderFactory} implementation which provides an appender that writes events to a MongoDB.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code type}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The appender type. Must be {@code mongodb}.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threshold}</td>
 *         <td>{@code ALL}</td>
 *         <td>The lowest level of events to print to the console.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code host}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>IP or host name of the server running the mongo daemon.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code host}</td>
 *         <td>27017</td>
 *         <td>Port of the mongo daemon.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code dbName}</td>
 *         <td><b>REQUIRED</b></td>
 *         <td>The name of the database in the mongodb.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code username}</td>
 *         <td><b>OPTIONAL</b></td>
 *         <td>The login username.If Auth is active.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code password}</td>
 *         <td><b>OPTIONAL</b></td>
 *         <td>The login password.If Auth is active.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code connectionsPerHost}</td>
 *         <td>10</td>
 *         <td>The maximum number of connections allowed per host for this Mongo instance.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code threadsAllowedToBlockForConnectionMultiplier}</td>
 *         <td>5</td>
 *         <td>This multiplier, multiplied with the connectionsPerHost setting, gives the maximum number of threads that may be waiting for a connection to become available from the pool.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxWaitTime}</td>
 *         <td>120000</td>
 *         <td>The maximum wait time in ms that a thread may wait for a connection to become available.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code connectTimeout}</td>
 *         <td>1000</td>
 *         <td>The connection timeout in milliseconds.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code socketTimeout}</td>
 *         <td>0</td>
 *         <td>The socket timeout in milliseconds It is used for I/O socket read and write operations Socket.setSoTimeout(int) Default is 0 and means no timeout.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code readPreference}</td>
 *         <td>secondaryPreferred</td>
 *         <td>Specify preferred replica set members to which a query or command can be sent.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code w}</td>
 *         <td>0</td>
 *         <td>Controls the acknowledgment of write operations with various options.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code wtimeout}</td>
 *         <td>0: indefinite</td>
 *         <td>how long to wait for slaves before failing</td>
 *     </tr>
 *     <tr>
 *         <td>{@code fsync}</td>
 *         <td>false</td>
 *         <td>If true and the server is running without journaling, blocks until the server has synced all data files to disk. If the server is running with journaling, this acts the same as the j option, blocking until write operations have been committed to the journal. Cannot be used in combination with j. In almost all cases the j flag should be used in preference to this one.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code capped}</td>
 *         <td>false</td>
 *         <td>If the targeted collection should be capped in number of documents or disk space.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code size}</td>
 *         <td>10000</td>
 *         <td>If {@code capped} {@code size} is the maximum number of documents in the collection.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code max}</td>
 *         <td>OPTIONAL</td>
 *         <td>If {@code capped} {@code max} is the maximum disk for the collection. The value can be expressed
 *             in bytes, kilobytes, megabytes, gigabytes, and terabytes by appending B, K, MB, GB, or TB to the
 *             numeric value.  Examples include 100MB, 1GB, 1TB.  Sizes can also be spelled out, such as 100 megabytes,
 *             1 gigabyte, 1 terabyte.</td>
 *     </tr>

 * </table>
 *
 * @see AbstractAppenderFactory
 */
@JsonTypeName("mongodb")
public class MongoDBAppenderFactory extends AbstractAppenderFactory {
	
	@NotNull
    private String host = "localhost";

	@NotNull
	@Min(1)
	@Max(65535)
    private int port = 27017;
    
	@NotNull
    private String dbName;


    private String username;

    private String password;
    
    @Min(1)
    private int connectionsPerHost = 10;

    private int threadsAllowedToBlockForConnectionMultiplier = 5;
    @Min(100)
    private int maxWaitTime = 120000;
    @Min(1)
    private int connectTimeout = 1000;
    @Min(0)
    private int socketTimeout = 0;

    @NotNull
    private String readPreference = "secondaryPreferred";

    @Min(0)
    private int w = 0;

    private int wtimeout =0;

    private boolean fsync = false;

    private boolean capped = false;

    private Size size;
    @Min(1)
    private long max = 10000;   

    @NotNull
    private String collectionName = "logging";
	
    @Override
	public Appender<ILoggingEvent> build(LoggerContext context,
			String applicationName, Layout<ILoggingEvent> layout) {
        final MongoDBAppender appender = new MongoDBAppender();
        appender.setName("mongodb-appender");
        appender.setContext(context);
        
        appender.setCapped(capped);
        appender.setCollectionName(collectionName);
        appender.setConnectionsPerHost(connectionsPerHost);
        appender.setConnectTimeout(connectTimeout);
        appender.setDbName(dbName);
        appender.setFsync(fsync);
        appender.setHost(host);
        appender.setMax(max);
        appender.setMaxWaitTime(maxWaitTime);
        appender.setPassword(password);
        appender.setPort(port);
        appender.setSize(size);
        appender.setReadPreference(ReadPreference.valueOf(readPreference));
        appender.setSocketTimeout(socketTimeout);
        appender.setThreadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
        appender.setUsername(username);
        appender.setW(w);
        appender.setWtimeout(wtimeout);
        
        
        addThresholdFilter(appender, threshold);
        appender.start();
        return wrapAsync(appender);
	}
	@JsonProperty
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	@JsonProperty
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	@JsonProperty
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	@JsonProperty
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	@JsonProperty
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	@JsonProperty
	public int getConnectionsPerHost() {
		return connectionsPerHost;
	}
	public void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}
	@JsonProperty
	public int getThreadsAllowedToBlockForConnectionMultiplier() {
		return threadsAllowedToBlockForConnectionMultiplier;
	}
	public void setThreadsAllowedToBlockForConnectionMultiplier(
			int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}
	@JsonProperty
	public int getMaxWaitTime() {
		return maxWaitTime;
	}
	public void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}
	@JsonProperty
	public int getConnectTimeout() {
		return connectTimeout;
	}
	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}
	@JsonProperty
	public int getSocketTimeout() {
		return socketTimeout;
	}
	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	@JsonProperty
	public int getW() {
		return w;
	}
	public void setW(int w) {
		this.w = w;
	}
	@JsonProperty
	public int getWtimeout() {
		return wtimeout;
	}
	public void setWtimeout(int wtimeout) {
		this.wtimeout = wtimeout;
	}
	@JsonProperty
	public boolean isFsync() {
		return fsync;
	}
	public void setFsync(boolean fsync) {
		this.fsync = fsync;
	}
	@JsonProperty
	public boolean isCapped() {
		return capped;
	}
	public void setCapped(boolean capped) {
		this.capped = capped;
	}
	@JsonProperty
	public Size getSize() {
		return size;
	}
	public void setSize(Size size) {
		this.size = size;
	}
	@JsonProperty
	public long getMax() {
		return max;
	}
	public void setMax(long max) {
		this.max = max;
	}
	@JsonProperty
	public String getCollectionName() {
		return collectionName;
	}
	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	@JsonProperty
	public String getReadPreference() {
		return readPreference;
	}
	public void setReadPreference(String readPreference) {
		this.readPreference = readPreference;
	}

}
