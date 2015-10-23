package io.dropwizard.jdbi;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JDBIOptionalTest {
    private final Environment env = new Environment("test", Jackson.newObjectMapper(), Validators.newValidator(), new MetricRegistry(), null);
    private final DataSourceFactory dataSourceFactory;
    private DBI dbi;

    public JDBIOptionalTest() {
        dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:" + JDBIOptionalTest.class + "-" + System.currentTimeMillis());
        dataSourceFactory.setUser("sa");
        dataSourceFactory.setPassword("");
    }

    @Before
    public void setupTests() throws IOException {
        dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        dbi.open().createStatement("CREATE TABLE IF NOT EXISTS test ( id INT PRIMARY KEY, time TIMESTAMP);").execute();
    }

    @Test
    public void testInsert() {
        final DateTime now = DateTime.now();
        final JDBIDao dao = dbi.onDemand(JDBIDao.class);
        dao.insert(1, Optional.of(now));

        final DateTime dateTime = dao.findById(1);
        assertThat(dateTime).isEqualTo(now);
    }

    @Test
    public void testAbsent() {
        final JDBIDao dao = dbi.onDemand(JDBIDao.class);
        dao.insert(1, Optional.<DateTime>absent());

        final DateTime dateTime = dao.findById(1);
        assertThat(dateTime).isNull();
    }

    public interface JDBIDao {
        @SqlUpdate("INSERT INTO test VALUES (:id, :ts)")
        void insert(@Bind("id") int id, @Bind("ts") Optional<DateTime> dt);

        @SqlQuery("SELECT time FROM test WHERE id = :id")
        DateTime findById(@Bind("id") int id);
    }
}
