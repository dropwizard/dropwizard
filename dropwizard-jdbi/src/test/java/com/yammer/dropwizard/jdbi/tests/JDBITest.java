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
import com.yammer.dropwizard.setup.AdminEnvironment;
import com.yammer.dropwizard.setup.LifecycleEnvironment;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.StringMapper;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class JDBITest {
    private final DatabaseConfiguration hsqlConfig = new DatabaseConfiguration();

    {
        LoggingFactory.bootstrap();
        hsqlConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.hsqldb.jdbcDriver");
        hsqlConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    }

    private final AdminEnvironment adminEnvironment = mock(AdminEnvironment.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final DBIFactory factory = new DBIFactory();
    private final List<Managed> managed = Lists.newArrayList();
    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        when(environment.getAdminEnvironment()).thenReturn(adminEnvironment);
        when(environment.getLifecycleEnvironment()).thenReturn(lifecycleEnvironment);

        this.dbi = factory.build(environment, hsqlConfig, "hsql");
        final ArgumentCaptor<Managed> managedCaptor = ArgumentCaptor.forClass(Managed.class);
        verify(lifecycleEnvironment).manage(managedCaptor.capture());
        managed.addAll(managedCaptor.getAllValues());
        for (Managed obj : managed) {
            obj.start();
        }

        final Handle handle = dbi.open();
        try {
            handle.createCall("DROP TABLE people IF EXISTS").invoke();
            handle.createCall(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(100), age int, created_at timestamp)")
                  .invoke();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?, ?)")
                  .bind(0, "Coda Hale")
                  .bind(1, "chale@yammer-inc.com")
                  .bind(2, 30)
                  .bind(3, new Timestamp(1365465078000L))
                  .execute();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?, ?)")
                  .bind(0, "Kris Gale")
                  .bind(1, "kgale@yammer-inc.com")
                  .bind(2, 32)
                  .bind(3, new Timestamp(1365466078000L))
                  .execute();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?, ?)")
                  .bind(0, "Old Guy")
                  .bindNull(1, Types.VARCHAR)
                  .bind(2, 99)
                  .bind(3, new Timestamp(1365467078000L))
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
        this.dbi = null;
    }

    @Test
    public void createsAValidDBI() throws Exception {
        final Handle handle = dbi.open();

        final Query<String> names = handle.createQuery("SELECT name FROM people WHERE age < ?")
                                          .bind(0, 50)
                                          .map(StringMapper.FIRST);
        assertThat(ImmutableList.copyOf(names))
                .containsOnly("Coda Hale", "Kris Gale");
    }

    @Test
    public void managesTheDatabaseWithTheEnvironment() throws Exception {
        verify(lifecycleEnvironment).manage(any(ManagedDataSource.class));
    }

    @Test
    public void sqlObjectsCanAcceptOptionalParams() throws Exception {
        final PersonDAO dao = dbi.open(PersonDAO.class);

        assertThat(dao.findByName(Optional.of("Coda Hale")))
                .isEqualTo("Coda Hale");
    }

    @Test
    public void sqlObjectsCanReturnImmutableLists() throws Exception {
        final PersonDAO dao = dbi.open(PersonDAO.class);

        assertThat(dao.findAllNames())
                .containsOnly("Coda Hale", "Kris Gale", "Old Guy");
    }

    @Test
    public void sqlObjectsCanReturnImmutableSets() throws Exception {
        final PersonDAO dao = dbi.open(PersonDAO.class);

        assertThat(dao.findAllUniqueNames())
                .containsOnly("Coda Hale", "Kris Gale", "Old Guy");
    }

    @Test
    public void sqlObjectsCanReturnOptional() throws Exception {
        final PersonDAO dao = dbi.open(PersonDAO.class);

        final Optional<String> found = dao.findByEmail("chale@yammer-inc.com");
        assertThat(found).isNotNull();
        assertThat(found.isPresent()).isTrue();
        assertThat(found.get()).isEqualTo("Coda Hale");


        final Optional<String> missing = dao.findByEmail("cemalettin.koc@gmail.com");
        assertThat(missing).isNotNull();
        assertThat(missing.isPresent()).isFalse();
        assertThat(missing.orNull()).isNull();
    }

    @Test
    public void sqlObjectsCanReturnJodaDateTime() throws Exception {
        final PersonDAO dao = dbi.open(PersonDAO.class);

        final DateTime found = dao.getLatestCreatedAt(new DateTime(1365465078000L));
        assertThat(found).isNotNull();
        assertThat(found.getMillis()).isEqualTo(1365467078000L);
        assertThat(found).isEqualTo(new DateTime(1365467078000L));
    }
}
