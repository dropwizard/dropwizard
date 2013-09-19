package io.dropwizard.jdbi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.LoggingFactory;
import io.dropwizard.setup.Environment;
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
import static org.mockito.Mockito.*;

public class JDBITest {
    private final DataSourceFactory hsqlConfig = new DataSourceFactory();

    {
        LoggingFactory.bootstrap();
        hsqlConfig.setUrl("jdbc:h2:mem:DbTest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.h2.Driver");
        hsqlConfig.setValidationQuery("SELECT 1");
    }

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final DBIFactory factory = new DBIFactory();
    private final List<Managed> managed = Lists.newArrayList();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        this.dbi = factory.build(environment, hsqlConfig, "hsql");
        final ArgumentCaptor<Managed> managedCaptor = ArgumentCaptor.forClass(Managed.class);
        verify(lifecycleEnvironment).manage(managedCaptor.capture());
        managed.addAll(managedCaptor.getAllValues());
        for (Managed obj : managed) {
            obj.start();
        }

        try (Handle handle = dbi.open()) {
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
                  .bind(3, new Timestamp(1365465078000L))
                  .execute();
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?, ?)")
                  .bind(0, "Old Guy")
                  .bindNull(1, Types.VARCHAR)
                  .bind(2, 99)
                  .bind(3, new Timestamp(1365465078000L))
                  .execute();
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

        final DateTime found = dao.getLatestCreatedAt(new DateTime(1365465077000L));
        assertThat(found).isNotNull();
        assertThat(found.getMillis()).isEqualTo(1365465078000L);
        assertThat(found).isEqualTo(new DateTime(1365465078000L));
    }
}
