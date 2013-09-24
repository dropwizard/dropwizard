package io.dropwizard.testing.junit;

import com.codahale.dropwizard.db.DataSourceFactory;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public abstract class DBIRule extends ExternalResource {
    private DBI dbi;

    private Handle handle;

    protected abstract DataSourceFactory getDatabaseConfiguration();

    protected abstract void setUpDataAccessObjects();

    public Handle getHandle() {
        return handle;
    }

    @Override
    protected void before() throws Throwable {
        dbi = new DBI(getDataSource());
        handle = dbi.open();
        setUpDataAccessObjects();
    }

    @Override
    protected void after() {
        handle.close();
    }

    protected <SqlObjectType> SqlObjectType onDemandDao(Class<SqlObjectType> sqlObjectType) {
        return dbi.onDemand(sqlObjectType);
    }

    private JdbcDataSource getDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL(getDatabaseConfiguration().getUrl());
        dataSource.setUser(getDatabaseConfiguration().getUser());
        dataSource.setPassword(getDatabaseConfiguration().getPassword());
        return dataSource;
    }
}
