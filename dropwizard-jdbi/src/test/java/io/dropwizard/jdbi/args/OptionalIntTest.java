package io.dropwizard.jdbi.args;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.io.IOException;
import java.util.OptionalInt;

import static org.assertj.core.api.Assertions.assertThat;

public class OptionalIntTest {
    private final Environment env = new Environment("test-optional-int", Jackson.newObjectMapper(),
        Validators.newValidator(), new MetricRegistry(), null);

    private TestDao dao;

    @Before
    public void setupTests() throws IOException {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:optional-int-" + System.currentTimeMillis() + "?user=sa");
        dataSourceFactory.setInitialSize(1);
        final DBI dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        try (Handle h = dbi.open()) {
            h.execute("CREATE TABLE test (id INT PRIMARY KEY, optional INT)");
        }
        dao = dbi.onDemand(TestDao.class);
    }

    @After
    public void tearDown() throws Exception {
        for (LifeCycle managedObject : env.lifecycle().getManagedObjects()) {
            managedObject.stop();
        }
    }

    @Test
    public void testPresent() {
        dao.insert(1, OptionalInt.of(42));

        assertThat(dao.findOptionalIntById(1).getAsInt()).isEqualTo(42);
    }

    @Test
    public void testAbsent() {
        dao.insert(2, OptionalInt.empty());

        assertThat(dao.findOptionalIntById(2).isPresent()).isFalse();
    }

    interface TestDao {

        @SqlUpdate("INSERT INTO test(id, optional) VALUES (:id, :optional)")
        void insert(@Bind("id") int id, @Bind("optional") OptionalInt optional);

        @SqlQuery("SELECT optional FROM test WHERE id = :id")
        OptionalInt findOptionalIntById(@Bind("id") int id);
    }
}
