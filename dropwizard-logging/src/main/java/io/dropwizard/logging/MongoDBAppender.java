package io.dropwizard.logging;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientOptions.Builder;
import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

public class MongoDBAppender extends UnsynchronizedAppenderBase<ILoggingEvent>{
	
    private DBCollection eventsCollection;
    private MongoClient mongoClient;
    
    private String host = "localhost";

    private int port = 27017;
    
    private String dbName = "db";

    private String collectionName;

    private String username;

    private String password;

    private int connectionsPerHost = 10;

    private int threadsAllowedToBlockForConnectionMultiplier = 5;

    private int maxWaitTime = 1000 * 60 * 2;

    private int connectTimeout;

    private int socketTimeout;

    private boolean slaveOk;

    private int w;

    private int wtimeout;

    private boolean fsync;

    private boolean capped = false;

    private long size = 10000;

    private long max = 10000;    
    
    @Override
	public void start() {
        try {
            connectToMongoDB();
            super.start();
        } catch (UnknownHostException e) {
            addError("Error connecting to MongoDB server: " + host + ":" + port, e);
        }
	}

	@Override
	public void stop() {
        if (mongoClient != null) {
            mongoClient.close();
        }
        super.stop();
	}

	protected BasicDBObject toMongoDocument(ILoggingEvent event) {
        final BasicDBObject doc = new BasicDBObject();
        doc.append("timestamp", new Date(event.getTimeStamp()));
        doc.append("logger", event.getLoggerName());
        doc.append("level", event.getLevel().levelStr);
        doc.append("thread", event.getThreadName());
        doc.append("mdc", event.getMDCPropertyMap());
        doc.append("message", event.getFormattedMessage());
        return doc;
    }

	@Override
	protected void append(ILoggingEvent eventObject) {
		eventsCollection.insert(toMongoDocument(eventObject));		
	}
	
    private void connectToMongoDB() throws UnknownHostException {

        if ((username != null) && (password != null)) {
            List<MongoCredential> credentialList = new ArrayList<>();
            credentialList.add(MongoCredential.createMongoCRCredential(username, dbName, password.toCharArray()));
            mongoClient = new MongoClient(new ServerAddress(host, port), credentialList, buildOptions());
        }
        else{
            mongoClient = new MongoClient(new ServerAddress(host, port), buildOptions());
        }
        final DB db = mongoClient.getDB(dbName);
        capIfNew(db);
        eventsCollection = db.getCollection(collectionName);
    }
    
    private MongoClientOptions buildOptions() {
        final Builder builder = MongoClientOptions.builder();
        builder.connectionsPerHost(connectionsPerHost);
        builder.threadsAllowedToBlockForConnectionMultiplier(threadsAllowedToBlockForConnectionMultiplier);
        builder.maxWaitTime(maxWaitTime);
        builder.connectTimeout(connectTimeout);
        builder.socketTimeout(socketTimeout);
        builder.writeConcern(new WriteConcern(w, wtimeout, fsync, false));
        builder.readPreference(slaveOk ? ReadPreference.secondaryPreferred() : ReadPreference.primaryPreferred());

        return builder.build();
    }
    
    private void capIfNew(DB db) {
        if(!db.getCollectionNames().contains(collectionName)) {
            BasicDBObject dbObject = new BasicDBObject("create", collectionName);
            dbObject.put("capped", capped);
            dbObject.put("size", size);
            dbObject.put("max", max);
            CommandResult cappedResult = db.command(dbObject);
        }
    }

	public void setHost(String host) {
		this.host = host;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setConnectionsPerHost(int connectionsPerHost) {
		this.connectionsPerHost = connectionsPerHost;
	}

	public void setThreadsAllowedToBlockForConnectionMultiplier(
			int threadsAllowedToBlockForConnectionMultiplier) {
		this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
	}

	public void setMaxWaitTime(int maxWaitTime) {
		this.maxWaitTime = maxWaitTime;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setSocketTimeout(int socketTimeout) {
		this.socketTimeout = socketTimeout;
	}

	public void setSlaveOk(boolean slaveOk) {
		this.slaveOk = slaveOk;
	}

	public void setW(int w) {
		this.w = w;
	}

	public void setWtimeout(int wtimeout) {
		this.wtimeout = wtimeout;
	}

	public void setFsync(boolean fsync) {
		this.fsync = fsync;
	}

	public void setCapped(boolean capped) {
		this.capped = capped;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setMax(long max) {
		this.max = max;
	}

}
