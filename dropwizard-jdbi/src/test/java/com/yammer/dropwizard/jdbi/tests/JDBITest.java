package com.yammer.dropwizard.jdbi.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.jdbi.DBIFactory;
import com.yammer.dropwizard.lifecycle.Managed;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.StringMapper;

import java.sql.Types;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class JDBITest {
    private final DatabaseConfiguration hsqlConfig = new DatabaseConfiguration();

    {
        LoggingFactory.bootstrap();
        hsqlConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.hsqldb.jdbcDriver");
        hsqlConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    }

    private final Environment environment = mock(Environment.class);
    private final DBIFactory factory = new DBIFactory(environment);
    private final List<Managed> managed = Lists.newArrayList();
    private DBI jdbi;

    @Before
    public void setUp() throws Exception {
        this.jdbi = factory.build(hsqlConfig, "hsql");
        final ArgumentCaptor<Managed> managedCaptor = ArgumentCaptor.forClass(Managed.class);
        verify(environment).manage(managedCaptor.capture());
        managed.addAll(managedCaptor.getAllValues());
        for (Managed obj : managed) {
            obj.start();
        }

        final Handle handle = jdbi.open();
        try {
            handle.createCall("DROP TABLE people IF EXISTS").invoke();
            handle.createCall(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(100), age int)")
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
        for (Managed obj : managed) {
            obj.stop();
        }
        this.jdbi = null;
    }

    @Test
    public void createsAValidDBI() throws Exception {
        final Handle handle = jdbi.open();
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
        verify(environment).manage(any(ManagedDataSource.class));
    }

    @Test
    public void sqlObjectsCanAcceptOptionalParams() throws Exception {
        final PersonDAO dao = jdbi.open(PersonDAO.class);
        try {
            assertThat(dao.findByName(Optional.of("Coda Hale")))
                    .isEqualTo("Coda Hale");
        } finally {
            jdbi.close(dao);
        }
    }

    @Test
    public void sqlObjectsCanReturnImmutableLists() throws Exception {
        final PersonDAO dao = jdbi.open(PersonDAO.class);
        try {
            assertThat(dao.findAllNames())
                    .containsOnly("Coda Hale", "Kris Gale", "Old Guy");
        } finally {
            jdbi.close(dao);
        }
    }

    @Test
    public void sqlObjectsCanReturnImmutableSets() throws Exception {
        final PersonDAO dao = jdbi.open(PersonDAO.class);
        try {
            assertThat(dao.findAllUniqueNames())
                    .containsOnly("Coda Hale", "Kris Gale", "Old Guy");
        } finally {
            jdbi.close(dao);
        }
    }

    @Test
    public void sqlObjectsCanReturnOptional() throws Exception {
        final PersonDAO dao = jdbi.open(PersonDAO.class);
        try {
            Optional<String> byEmail = dao.findByEmail("chale@yammer-inc.com");
            assertThat(byEmail).isNotNull();
            assertThat(byEmail.get()).isEqualTo("Coda Hale");


            byEmail = dao.findByEmail("cemalettin.koc@gmail.com");
            assertThat(byEmail).isNotNull();
            assertThat(byEmail).isEqualTo(Optional.<String>absent());
            assertThat(byEmail.orNull()).isNull();

        } finally {
            jdbi.close(dao);
        }
    }
}
