package com.codahale.dropwizard.db;

import com.codahale.dropwizard.lifecycle.Managed;

import javax.sql.DataSource;

public interface ManagedDataSource extends DataSource, Managed {

}
