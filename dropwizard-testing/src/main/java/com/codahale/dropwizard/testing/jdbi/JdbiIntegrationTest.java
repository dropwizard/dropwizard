package com.codahale.dropwizard.testing.jdbi;

import com.codahale.dropwizard.db.DataSourceFactory;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

import java.sql.SQLException;

/**
 * A base test class for lightweight integration testing of JDBI SQL Objects.
 */
public abstract class JdbiIntegrationTest {

    private DBI dbi;

    private Handle handle;

    private Liquibase liquibase;

    @Before
    public void setUpDatabase() throws Exception {
        dbi = new DBI(getDataSource());
        handle = dbi.open();
        migrateDatabase();
        setUpDataAccessObjects();
    }

    protected abstract DataSourceFactory getDatabaseConfiguration();

    protected abstract void setUpDataAccessObjects();

    @After
    public void tearDown() throws Exception {
        liquibase.dropAll();
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

    private void migrateDatabase() throws LiquibaseException, SQLException, ClassNotFoundException {
        liquibase = new Liquibase("migrations.xml", new ClassLoaderResourceAccessor(), new JdbcConnection(handle.getConnection()));
        liquibase.update(null);
    }
}