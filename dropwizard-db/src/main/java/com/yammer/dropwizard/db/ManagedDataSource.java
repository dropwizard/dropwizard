package com.yammer.dropwizard.db;

import com.yammer.dropwizard.lifecycle.Managed;

import javax.sql.DataSource;

public interface ManagedDataSource extends DataSource, Managed {

}
