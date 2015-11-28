package io.dropwizard.jdbi.timestamps;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.util.component.LifeCycle;
import org.junit.rules.ExternalResource;
import org.skife.jdbi.v2.DBI;

import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Configured JDBI client for the database
 */
public class DBIClient extends ExternalResource {

    private final TimeZone dbTimeZone;

    private DBI dbi;
    private List<LifeCycle> managedObjects;

    public DBIClient(TimeZone dbTimeZone) {
        this.dbTimeZone = dbTimeZone;
    }

    public DBI getDbi() {
        return dbi;
    }

    @Override
    protected void before() throws Throwable {
        final Environment environment = new Environment("test", Jackson.newObjectMapper(),
                Validators.newValidator(), new MetricRegistry(),
                getClass().getClassLoader());

        final DataSourceFactory dataSourceFactory = new DataSourceFactory();
        dataSourceFactory.setDriverClass("org.h2.Driver");
        dataSourceFactory.setUrl("jdbc:h2:tcp://localhost/fldb");
        dataSourceFactory.setUser("sa");
        dataSourceFactory.setPassword("");

        // Set the time zone of the database
        final DBIFactory dbiFactory = new DBIFactory() {
            @Override
            protected Optional<TimeZone> databaseTimeZone() {
                return Optional.of(dbTimeZone);
            }
        };
        dbi = dbiFactory.build(environment, dataSourceFactory, "test-jdbi-time-zones");

        // Start the DB pool
        managedObjects = environment.lifecycle().getManagedObjects();
        for (LifeCycle managedObject : managedObjects) {
            managedObject.start();
        }
    }

    @Override
    protected void after() {
        // Shutdown the DB pool
        try {
            for (LifeCycle managedObject : managedObjects) {
                managedObject.stop();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
