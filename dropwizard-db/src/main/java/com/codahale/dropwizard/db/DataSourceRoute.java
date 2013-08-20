package com.codahale.dropwizard.db;

import javax.validation.constraints.NotNull;

/**
 * Route-capable {@link DataSourceFactory}.
 */
public class DataSourceRoute {
    @NotNull
    private String routeName;

    @NotNull
    private DataSourceFactory database;

    /**
     * @return the routeName
     */
    public String getRouteName() {
        return routeName;
    }

    /**
     * @param routeName
     *            the routeName to set
     */
    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    /**
     * @return the database
     */
    public DataSourceFactory getDatabase() {
        return database;
    }

    /**
     * @param database
     *            the database to set
     */
    public void setDatabase(DataSourceFactory database) {
        this.database = database;
    }
}
