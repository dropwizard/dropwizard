package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.jersey.jackson.JacksonFeature;
import io.dropwizard.jersey.optional.EmptyOptionalExceptionMapper;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.common.BootstrapLogging;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.h2.jdbc.JdbcConnection;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JerseyIntegrationTest extends JerseyTest {
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

    @Nullable
    private SessionFactory sessionFactory;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();

        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    protected Application configure() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        final SessionFactoryFactory factory = new SessionFactoryFactory();
        final DataSourceFactory dbConfig = new DataSourceFactory();
        dbConfig.setProperties(Collections.singletonMap("hibernate.jdbc.time_zone", "UTC"));

        final HibernateBundle<?> bundle = mock(HibernateBundle.class);
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);

        dbConfig.setUrl("jdbc:h2:mem:DbTest-" + System.nanoTime());
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.h2.Driver");
        dbConfig.setValidationQuery("SELECT 1");

        this.sessionFactory = factory.build(bundle,
                                            environment,
                                            dbConfig,
                                            Collections.singletonList(Person.class));

        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createNativeQuery(
                "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                .executeUpdate();
            session.createNativeQuery(
                "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                .executeUpdate();
            transaction.commit();
        }

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting();
        config.register(new UnitOfWorkApplicationListener("hr-db", sessionFactory));
        config.register(new PersonResource(new PersonDAO(sessionFactory)));
        config.register(new PersistenceExceptionMapper());
        config.register(new JacksonFeature(Jackson.newObjectMapper()));
        config.register(new DataExceptionMapper());
        config.register(new EmptyOptionalExceptionMapper());
        config.register(new JdbcSQLNonTransientConnectionExceptionMapper());

        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonFeature(Jackson.newObjectMapper()));
    }

    @Test
    void findsExistingData() {
        final Person coda = target("/people/Coda").request(MediaType.APPLICATION_JSON).get(Person.class);

        assertThat(coda.getName())
                .isEqualTo("Coda");

        assertThat(coda.getEmail())
                .isEqualTo("coda@example.com");

        assertThat(coda.getBirthday())
                .isEqualTo(ZonedDateTime.of(1979, 1, 2, 0, 22, 0, 0, ZoneId.of("UTC")));
    }

    @Test
    void doesNotFindMissingData() {
        Invocation.Builder request = target("/people/Poof").request(MediaType.APPLICATION_JSON);
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(Person.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(404));
    }

    @Test
    void createsNewData() {
        final Person person = new Person();
        person.setName("Hank");
        person.setEmail("hank@example.com");
        person.setBirthday(ZonedDateTime.of(1971,3, 14, 14, 19, 12, 0, ZoneId.of("UTC")));

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
    void testSqlExceptionIsHandled() {
        final Person person = new Person();
        person.setName("Jeff");
        person.setEmail("jeff.hammersmith@targetprocessinc.com");
        person.setBirthday(ZonedDateTime.of(1984, 2, 11, 0, 0, 0, 0, ZoneId.of("UTC")));

        final Response response = target("/people/Jeff").request().
                put(Entity.entity(person, MediaType.APPLICATION_JSON));

        assertThat(response.getStatusInfo()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.readEntity(ErrorMessage.class).getMessage()).isEqualTo("Wrong email");
    }

    @Test
    void testBeforeStartExceptionIsMapped() {
        // closes the H2 Session
        // then no connections can be established to the db and a JdbcSQLNonTransientConnectionException is thrown
        try (Session session = Objects.requireNonNull(sessionFactory).openSession()) {
            session.doWork(connection -> connection.unwrap(JdbcConnection.class).getSession().close());
        }
        Response response = target("/people/Coda").request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus()).isEqualTo(Response.Status.SERVICE_UNAVAILABLE.getStatusCode());
        Map<String, List<String>> mappedStackTraceElements = response.readEntity(new GenericType<>() {});
        assertThat(mappedStackTraceElements).isNotNull().isNotEmpty();
        // ensure, that the exception is thrown by the beforeStart method of the UnitOfWorkAspect class
        boolean isThrownByBeforeStartMethod = mappedStackTraceElements.containsKey(UnitOfWorkAspect.class.getName())
            && mappedStackTraceElements.get(UnitOfWorkAspect.class.getName()).contains("beforeStart");
        assertThat(isThrownByBeforeStartMethod).isTrue();
    }
}
