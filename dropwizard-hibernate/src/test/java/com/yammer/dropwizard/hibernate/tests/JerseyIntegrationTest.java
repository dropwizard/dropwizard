package com.yammer.dropwizard.hibernate.tests;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.AbstractDAO;
import com.yammer.dropwizard.hibernate.SessionFactoryFactory;
import com.yammer.dropwizard.hibernate.UnitOfWork;
import com.yammer.dropwizard.hibernate.UnitOfWorkResourceMethodDispatchAdapter;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.After;
import org.junit.Test;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.mock;

public class JerseyIntegrationTest extends JerseyTest {
    static {
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
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

    @Override
    @After
    public void tearDown() throws Exception {
        super.tearDown();

        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @Override
    protected AppDescriptor configure() {
        final SessionFactoryFactory factory = new SessionFactoryFactory();
        final DatabaseConfiguration dbConfig = new DatabaseConfiguration();
        final ImmutableList<String> packages = ImmutableList.of("com.yammer.dropwizard.hibernate.tests");
        final Environment environment = mock(Environment.class);

        dbConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.nanoTime());
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        try {
            this.sessionFactory = factory.build(environment,
                                                dbConfig,
                                                ImmutableList.<Class<?>>of(Person.class));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        final Session session = sessionFactory.openSession();
        try {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery("CREATE TABLE people (name varchar(100) primary key, email varchar(100), birthday timestamp)").executeUpdate();
            session.createSQLQuery("INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00')").executeUpdate();
        } finally {
            session.close();
        }

        final DropwizardResourceConfig config = new DropwizardResourceConfig(true);
        config.getSingletons().add(new UnitOfWorkResourceMethodDispatchAdapter(sessionFactory));
        config.getSingletons().add(new PersonResource(new PersonDAO(sessionFactory)));
        config.getSingletons().add(new JacksonMessageBodyProvider(new ObjectMapperFactory().build(),
                                                                  new Validator()));
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
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
