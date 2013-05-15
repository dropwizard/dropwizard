package com.codahale.dropwizard.hibernate;

import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Test;

import javax.validation.Validation;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.TimeZone;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyIntegrationTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    public static class PersonDAO extends AbstractDAO<Person> {
        public PersonDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        public Optional<Person> findByName(String name) {
            return Optional.fromNullable(get(name));
        }

        @Override
        public Person persist(Person entity) {
            return super.persist(entity);
        }
    }

    @Path("/people/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public static class PersonResource {
        private final PersonDAO dao;

        public PersonResource(PersonDAO dao) {
            this.dao = dao;
        }

        @GET
        @UnitOfWork(readOnly = true)
        public Optional<Person> find(@PathParam("name") String name) {
            return dao.findByName(name);
        }

        @PUT
        @UnitOfWork
        public void save(Person person) {
            dao.persist(person);
        }
    }

    private SessionFactory sessionFactory;
    private TimeZone defaultTZ;

    @Override
    @After
    public void tearDown() throws Exception {
        TimeZone.setDefault(defaultTZ);
        super.tearDown();

        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        this.defaultTZ = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Override
    protected AppDescriptor configure() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final SessionFactoryFactory factory = new SessionFactoryFactory();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        final HibernateBundle<?> bundle = mock(HibernateBundle.class);
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        dbConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.nanoTime());
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        try {
            this.sessionFactory = factory.build(bundle,
                                                environment,
                                                dbConfig,
                                                ImmutableList.<Class<?>>of(Person.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final Session session = sessionFactory.openSession();
        try {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(100), birthday timestamp with time zone)")
                   .executeUpdate();
            session.createSQLQuery(
                    "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                   .executeUpdate();
        } finally {
            session.close();
        }

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        config.getSingletons().add(new UnitOfWorkResourceMethodDispatchAdapter(sessionFactory));
        config.getSingletons().add(new PersonResource(new PersonDAO(sessionFactory)));
        config.getSingletons().add(new JacksonMessageBodyProvider(Jackson.newObjectMapper(),
                                                                  Validation.buildDefaultValidatorFactory().getValidator()));
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void findsExistingData() throws Exception {
        final Person coda = client().resource("/people/Coda")
                .accept(MediaType.APPLICATION_JSON)
                .get(Person.class);

        assertThat(coda.getName())
                .isEqualTo("Coda");

        assertThat(coda.getEmail())
                .isEqualTo("coda@example.com");

        assertThat(coda.getBirthday())
                .isEqualTo(new DateTime(1979, 1, 2, 0, 22, DateTimeZone.UTC));
    }

    @Test
    public void doesNotFindMissingData() throws Exception {
        try {
            client().resource("/people/Poof")
                    .accept(MediaType.APPLICATION_JSON)
                    .get(Person.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(404);
        }
    }

    @Test
    public void createsNewData() throws Exception {
        final Person person = new Person();
        person.setName("Hank");
        person.setEmail("hank@example.com");
        person.setBirthday(new DateTime(1971, 3, 14, 19, 12, DateTimeZone.UTC));

        client().resource("/people/Hank").type(MediaType.APPLICATION_JSON).put(person);

        final Person hank = client().resource("/people/Hank")
                .accept(MediaType.APPLICATION_JSON)
                .get(Person.class);

        assertThat(hank.getName())
                .isEqualTo("Hank");

        assertThat(hank.getEmail())
                .isEqualTo("hank@example.com");

        assertThat(hank.getBirthday())
                .isEqualTo(person.getBirthday());
    }
}
