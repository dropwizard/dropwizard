package io.dropwizard.logging;

import java.util.Locale;
import java.util.regex.Matcher;

import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.mongodb.MongoClient;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;


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
 *         <td>{@code slaveOk}</td>
 *         <td>true</td>
 *         <td>Reading from secondary is allowed when in a replication set.</td>
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
 *         <td>10000</td>
 *         <td>If {@code capped} {@code max} is the maximum disk for the collection.</td>
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
    private int port = 27017;
    
	@NotNull
    private String dbName;


    private String username;

    private String password;
    

    private int connectionsPerHost = 10;

    private int threadsAllowedToBlockForConnectionMultiplier = 5;

    private int maxWaitTime = 1000 * 60 * 2;

    private int connectTimeout = 1000;

    private int socketTimeout = 1000;

    private boolean slaveOk = true;

    private int w = 0;

    private int wtimeout =1000;

    private boolean fsync = false;

    private boolean capped = false;

    private long size = 10000;

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
        appender.setSlaveOk(slaveOk);
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
	public boolean isSlaveOk() {
		return slaveOk;
	}
	public void setSlaveOk(boolean slaveOk) {
		this.slaveOk = slaveOk;
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
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
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

}
