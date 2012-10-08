package com.yammer.dropwizard.db.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.db.Database;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.DatabaseFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.StringMapper;

import java.sql.SQLException;
import java.sql.Types;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DatabaseTest {
    private final DatabaseConfiguration hsqlConfig = new DatabaseConfiguration();

    {
        LoggingFactory.bootstrap();
        hsqlConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.hsqldb.jdbcDriver");
        hsqlConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    }

    private final Environment environment = mock(Environment.class);
    private final DatabaseFactory factory = new DatabaseFactory(environment);
    private Database database;

    @Before
    public void setUp() throws Exception {
        this.database = factory.build(hsqlConfig, "hsql");
        final Handle handle = database.open();
        try {
            handle.createCall("DROP TABLE people IF EXISTS").invoke();
            handle.createCall("CREATE TABLE people (name varchar(100) primary key, email varchar(100), age int)")
                  .invoke();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
                  .bind(0, "Coda Hale")
                  .bind(1, "chale@yammer-inc.com")
                  .bind(2, 30)
                  .execute();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
                  .bind(0, "Kris Gale")
                  .bind(1, "kgale@yammer-inc.com")
                  .bind(2, 32)
                  .execute();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?)")
                  .bind(0, "Old Guy")
                  .bindNull(1, Types.VARCHAR)
                  .bind(2, 99)
                  .execute();
        } finally {
            handle.close();
        }
    }

    @After
    public void tearDown() throws Exception {
        database.stop();
        this.database = null;
    }

    @Test
    public void createsAValidDBI() throws Exception {
        final Handle handle = database.open();
        try {
            final Query<String> names = handle.createQuery("SELECT name FROM people WHERE age < ?")
                                              .bind(0, 50)
                                              .map(StringMapper.FIRST);
            assertThat(ImmutableList.copyOf(names))
                    .containsOnly("Coda Hale", "Kris Gale");
        } finally {
            handle.close();
        }
    }

    @Test
    public void managesTheDatabaseWithTheEnvironment() throws Exception {
        final Database db = factory.build(hsqlConfig, "hsql");

        verify(environment).manage(db);
    }

    @Test
    public void sqlObjectsCanAcceptOptionalParams() throws Exception {
        final PersonDAO dao = database.open(PersonDAO.class);
        try {
            assertThat(dao.findByName(Optional.of("Coda Hale")))
                    .isEqualTo("Coda Hale");
        } finally {
            database.close(dao);
        }
    }

    @Test
    public void sqlObjectsCanReturnImmutableLists() throws Exception {
        final PersonDAO dao = database.open(PersonDAO.class);
        try {
            assertThat(dao.findAllNames())
                    .containsOnly("Coda Hale", "Kris Gale", "Old Guy");
        } finally {
            database.close(dao);
        }
    }

    @Test
    @SuppressWarnings("CallToPrintStackTrace")
    public void pingWorks() throws Exception {
        try {
            database.ping();
        } catch (SQLException e) {
            e.printStackTrace();
            fail("shouldn't have thrown an exception but did");
        }
    }
}
