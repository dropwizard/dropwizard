package com.yammer.dropwizard.db;

import javax.sql.DataSource;
import java.io.Closeable;

public interface ClosableDataSource extends DataSource, Closeable {

}
