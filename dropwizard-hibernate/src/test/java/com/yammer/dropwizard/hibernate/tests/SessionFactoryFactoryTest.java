package com.yammer.dropwizard.hibernate.tests;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.ManagedSessionFactory;
import com.yammer.dropwizard.hibernate.SessionFactoryFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionFactoryFactoryTest {
    static {
        ((Logger) LoggerFactory.getLogger("org")).setLevel(Level.OFF);
    }

    private final SessionFactoryFactory factory = new SessionFactoryFactory();

    private final Environment environment = mock(Environment.class);
    private final DatabaseConfiguration config = new DatabaseConfiguration();
    private final ImmutableList<String> packages = ImmutableList.of("com.yammer.dropwizard.hibernate.tests");

    private SessionFactory sessionFactory;

    @Before
    public void setUp() throws Exception {
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

        verify(environment).manage(any(ManagedSessionFactory.class));
    }

    @Test
    public void buildsAWorkingSessionFactory() throws Exception {
        build();

        final Session session = sessionFactory.openSession();
        try {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery("CREATE TABLE people (name varchar(100) primary key, email varchar(100), age int)").executeUpdate();
            session.createSQLQuery("INSERT INTO people VALUES ('Coda', 'coda@example.com', 300)").executeUpdate();

            final ExampleEntity entity = (ExampleEntity) session.get(ExampleEntity.class, "Coda");

            assertThat(entity.getName())
                    .isEqualTo("Coda");

            assertThat(entity.getEmail())
                    .isEqualTo("coda@example.com");

            assertThat(entity.getAge())
                    .isEqualTo(300);
        } finally {
            session.close();
        }
    }

    private void build() throws ClassNotFoundException {
        this.sessionFactory = factory.build(environment, config, packages);
    }
}
