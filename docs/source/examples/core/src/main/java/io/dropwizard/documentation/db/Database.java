package io.dropwizard.documentation.db;

public final class Database {
    public boolean isConnected() {
        return true;
    }

    public String getUrl() {
        return "database-url";
    }

    public void truncate() {}
}
