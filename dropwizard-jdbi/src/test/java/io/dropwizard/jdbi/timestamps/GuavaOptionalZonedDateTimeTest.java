package io.dropwizard.jdbi.timestamps;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class GuavaOptionalZonedDateTimeTest {

    private final Environment env = new Environment("test-guava-zoned-date-time", Jackson.newObjectMapper(),
            Validators.newValidator(), new MetricRegistry(), null);

    private TaskDao dao;

    @Before
    public void setupTests() throws IOException {
        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:mem:guava-zoned-date-time-" + System.currentTimeMillis() + "?user=sa");
        dataSourceFactory.setInitialSize(1);
        final DBI dbi = new DBIFactory().build(env, dataSourceFactory, "test");
        try (Handle h = dbi.open()) {
            h.execute("CREATE TABLE tasks (" +
                    "id INT PRIMARY KEY, " +
                    "assignee VARCHAR(255) NOT NULL, " +
                    "start_date TIMESTAMP, " +
                    "end_date TIMESTAMP, " +
                    "comments VARCHAR(1024) " +
                    ")");
        }
        dao = dbi.onDemand(TaskDao.class);
    }

    @Test
    public void testPresent() {
        final ZonedDateTime startDate = ZonedDateTime.now();
        final ZonedDateTime endDate = startDate.plusDays(1L);
        dao.insert(1, Optional.of("John Hughes"), startDate, Optional.of(endDate), Optional.<String>absent());

        assertThat(dao.findEndDateById(1).get()).isEqualTo(endDate);
    }

    @Test
    public void testAbsent() {
        dao.insert(2, Optional.of("Kate Johansen"), ZonedDateTime.now(),
                Optional.<ZonedDateTime>absent(), Optional.of("To be done"));

        assertThat(dao.findEndDateById(2).isPresent()).isFalse();
    }

    interface TaskDao {

        @SqlUpdate("INSERT INTO tasks(id, assignee, start_date, end_date, comments) " +
                "VALUES (:id, :assignee, :start_date, :end_date, :comments)")
        void insert(@Bind("id") int id, @Bind("assignee") Optional<String> assignee,
                    @Bind("start_date") ZonedDateTime startDate, @Bind("end_date") Optional<ZonedDateTime> endDate,
                    @Bind("comments") Optional<String> comments);

        @SqlQuery("SELECT end_date FROM tasks WHERE id = :id")
        @SingleValueResult
        Optional<ZonedDateTime> findEndDateById(@Bind("id") int id);
    }
}
