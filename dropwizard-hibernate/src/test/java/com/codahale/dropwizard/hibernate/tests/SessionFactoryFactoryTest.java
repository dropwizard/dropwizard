package com.codahale.dropwizard.hibernate.tests;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import com.codahale.dropwizard.config.Environment;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.codahale.dropwizard.hibernate.HibernateBundle;
import com.codahale.dropwizard.hibernate.ManagedSessionFactory;
import com.codahale.dropwizard.hibernate.SessionFactoryFactory;
import com.codahale.dropwizard.setup.LifecycleEnvironment;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionFactoryFactoryTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
    }

    private final SessionFactoryFactory factory = new SessionFactoryFactory();

    private final HibernateBundle<?> bundle = mock(HibernateBundle.class);
    private final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
    private final Environment environment = mock(Environment.class);
    private final DatabaseConfiguration config = new DatabaseConfiguration();
    private final MetricRegistry metricRegistry = new MetricRegistry();

    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
        when(environment.getMetricRegistry()).thenReturn(metricRegistry);
        when(environment.getLifecycleEnvironment()).thenReturn(lifecycleEnvironment);

        config.setUrl("jdbc:hsqldb:mem:DbTest-" + System.currentTimeMillis());
        config.setUser("sa");
        config.setDriverClass("org.hsqldb.jdbcDriver");
        config.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");
    }

    @After
    public void tearDown() throws Exception {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Test
    public void managesTheSessionFactory() throws Exception {
        build();

        verify(lifecycleEnvironment).manage(any(ManagedSessionFactory.class));
    }

    @Test
    public void callsBundleToConfigure() throws Exception {
      build();

      verify(bundle).configure(any(Configuration.class));
    }

    @Test
    public void buildsAWorkingSessionFactory() throws Exception {
        build();

        final Session session = sessionFactory.openSession();
        try {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery("CREATE TABLE people (name varchar(100) primary key, email varchar(100), birthday timestamp)").executeUpdate();
            session.createSQLQuery("INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00')").executeUpdate();

            final Person entity = (Person) session.get(Person.class, "Coda");

            assertThat(entity.getName())
                    .isEqualTo("Coda");

            assertThat(entity.getEmail())
                    .isEqualTo("coda@example.com");

            assertThat(entity.getBirthday().toDateTime(DateTimeZone.UTC))
                    .isEqualTo(new DateTime(1979, 1, 2, 0, 22, DateTimeZone.UTC));
        } finally {
            session.close();
        }
    }

    private void build() throws ClassNotFoundException {
        this.sessionFactory = factory.build(bundle,
                                            environment,
                                            config,
                                            ImmutableList.<Class<?>>of(Person.class));
    }
}
