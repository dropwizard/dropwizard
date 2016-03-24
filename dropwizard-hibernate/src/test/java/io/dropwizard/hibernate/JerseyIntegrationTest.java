package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JerseyIntegrationTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    public static class PersonDAO extends AbstractDAO<Person> {
        public PersonDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        public Optional<Person> findByName(String name) {
            return Optional.ofNullable(get(name));
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

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    protected Application configure() {
        forceSet(TestProperties.CONTAINER_PORT, "0");

        final MetricRegistry metricRegistry = new MetricRegistry();
        final SessionFactoryFactory factory = new SessionFactoryFactory();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        final HibernateBundle<?> bundle = mock(HibernateBundle.class);
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        dbConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.nanoTime()+"?hsqldb.translate_dti_types=false");
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        this.sessionFactory = factory.build(bundle,
                                            environment,
                                            dbConfig,
                                            ImmutableList.<Class<?>>of(Person.class));

        final Session session = sessionFactory.openSession();
        try {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                   .executeUpdate();
            session.createSQLQuery(
                    "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                   .executeUpdate();
        } finally {
            session.close();
        }

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        config.register(new UnitOfWorkApplicationListener("hr-db", sessionFactory));
        config.register(new PersonResource(new PersonDAO(sessionFactory)));
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper()));
        config.register(new DataExceptionMapper());

        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper()));
    }

    @Test
    public void findsExistingData() throws Exception {
        final Person coda = target("/people/Coda").request(MediaType.APPLICATION_JSON).get(Person.class);

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
            target("/people/Poof").request(MediaType.APPLICATION_JSON)
                    .get(Person.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
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

        target("/people/Hank").request().put(Entity.entity(person, MediaType.APPLICATION_JSON));

        final Person hank = target("/people/Hank")
                .request(MediaType.APPLICATION_JSON)
                .get(Person.class);

        assertThat(hank.getName())
                .isEqualTo("Hank");

        assertThat(hank.getEmail())
                .isEqualTo("hank@example.com");

        assertThat(hank.getBirthday())
                .isEqualTo(person.getBirthday());
    }


    @Test
    public void testSqlExceptionIsHandled() throws Exception {
        final Person person = new Person();
        person.setName("Jeff");
        person.setEmail("jeff.hammersmith@targetprocessinc.com");
        person.setBirthday(new DateTime(1984, 2, 11, 0, 0, DateTimeZone.UTC));

        final Response response = target("/people/Jeff").request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.readEntity(ErrorMessage.class).getMessage()).isEqualTo("Wrong email");
    }
}
