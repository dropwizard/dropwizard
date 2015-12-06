package io.dropwizard.jdbi;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.google.common.base.Optional;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.Query;
import org.skife.jdbi.v2.util.StringColumnMapper;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GuavaJDBITest {
    private final DataSourceFactory hsqlConfig = new DataSourceFactory();

    {
        BootstrapLogging.bootstrap();
        hsqlConfig.setUrl("jdbc:h2:mem:GuavaJDBITest-" + System.currentTimeMillis());
        hsqlConfig.setUser("sa");
        hsqlConfig.setDriverClass("org.h2.Driver");
        hsqlConfig.setValidationQuery("SELECT 1");
    }

    private final HealthCheckRegistry healthChecks = mock(HealthCheckRegistry.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final DBIFactory factory = new DBIFactory();
    private final List<Managed> managed = new ArrayList<>();
    private final MetricRegistry metricRegistry = new MetricRegistry();
    private DBI dbi;

    @Before
    public void setUp() throws Exception {
        when(environment.healthChecks()).thenReturn(healthChecks);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);
        when(environment.getHealthCheckExecutorService()).thenReturn(Executors.newSingleThreadExecutor());

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
            handle.createStatement("INSERT INTO people VALUES (?, ?, ?, ?)")
                  .bind(0, "Alice Example")
                  .bind(1, "alice@example.org")
                  .bind(2, 99)
                  .bindNull(3, Types.TIMESTAMP)
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
                                          .map(StringColumnMapper.INSTANCE);
        assertThat(names).containsOnly("Coda Hale", "Kris Gale");
    }

    @Test
    public void managesTheDatabaseWithTheEnvironment() throws Exception {
        verify(lifecycleEnvironment).manage(any(ManagedDataSource.class));
    }

    @Test
    public void sqlObjectsCanAcceptOptionalParams() throws Exception {
        final GuavaPersonDAO dao = dbi.open(GuavaPersonDAO.class);

        assertThat(dao.findByName(Optional.of("Coda Hale")))
                .isEqualTo("Coda Hale");
    }

    @Test
    public void sqlObjectsCanReturnImmutableLists() throws Exception {
        final GuavaPersonDAO dao = dbi.open(GuavaPersonDAO.class);

        assertThat(dao.findAllNames())
                .containsOnly("Coda Hale", "Kris Gale", "Old Guy", "Alice Example");
    }

    @Test
    public void sqlObjectsCanReturnImmutableSets() throws Exception {
        final GuavaPersonDAO dao = dbi.open(GuavaPersonDAO.class);

        assertThat(dao.findAllUniqueNames())
                .containsOnly("Coda Hale", "Kris Gale", "Old Guy", "Alice Example");
    }

    @Test
    public void sqlObjectsCanReturnOptional() throws Exception {
        final GuavaPersonDAO dao = dbi.open(GuavaPersonDAO.class);

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
        final GuavaPersonDAO dao = dbi.open(GuavaPersonDAO.class);

        final DateTime found = dao.getLatestCreatedAt(new DateTime(1365465077000L));
        assertThat(found).isNotNull();
        assertThat(found.getMillis()).isEqualTo(1365465078000L);
        assertThat(found).isEqualTo(new DateTime(1365465078000L));

        final DateTime notFound = dao.getCreatedAtByEmail("alice@example.org");
        assertThat(notFound).isNull();

        final Optional<DateTime> absentDateTime = dao.getCreatedAtByName("Alice Example");
        assertThat(absentDateTime).isNotNull();
        assertThat(absentDateTime.isPresent()).isFalse();

        final Optional<DateTime> presentDateTime = dao.getCreatedAtByName("Coda Hale");
        assertThat(presentDateTime).isNotNull();
        assertThat(presentDateTime.isPresent()).isTrue();
    }
}
