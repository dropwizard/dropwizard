package io.dropwizard.db;

import io.dropwizard.lifecycle.Managed;

import javax.sql.DataSource;

public interface ManagedDataSource extends DataSource, Managed {

}
