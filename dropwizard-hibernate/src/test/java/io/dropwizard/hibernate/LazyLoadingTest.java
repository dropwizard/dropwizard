package io.dropwizard.hibernate;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LazyLoadingTest extends JerseyTest {

    private Bootstrap<?> bootstrap;
    private HibernateBundle<Configuration> bundle;

    static {
        BootstrapLogging.bootstrap();
    }

    public static class DogDAO extends AbstractDAO<Dog> {
        public DogDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        public Optional<Dog> findByName(String name) {
            return Optional.fromNullable(get(name));
        }
    }

    @Path("/dogs/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public static class DogResource {
        private final DogDAO dao;

        public DogResource(DogDAO dao) {
            this.dao = dao;
        }

        @GET
        @UnitOfWork(readOnly = true)
        public Optional<Dog> find(@PathParam("name") String name) {
            return dao.findByName(name);
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
        final Environment environment = mock(Environment.class);
        final LifecycleEnvironment lifecycleEnvironment = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycleEnvironment);
        when(environment.metrics()).thenReturn(metricRegistry);
        bundle = new HibernateBundle<Configuration>(null, factory) {
            @Override
            public DataSourceFactory getDataSourceFactory(Configuration configuration) {
                return dbConfig;
            }
        };

        dbConfig.setUrl("jdbc:hsqldb:mem:DbTest-" + System.nanoTime()+"?hsqldb.translate_dti_types=false");
        dbConfig.setUser("sa");
        dbConfig.setDriverClass("org.hsqldb.jdbcDriver");
        dbConfig.setValidationQuery("SELECT 1 FROM INFORMATION_SCHEMA.SYSTEM_USERS");

        this.sessionFactory = factory.build(bundle,
                                            environment,
                                            dbConfig,
                                            ImmutableList.of(Person.class, Dog.class));

        try (Session session = sessionFactory.openSession()) {
            session.createSQLQuery("DROP TABLE people IF EXISTS").executeUpdate();
            session.createSQLQuery(
                "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                .executeUpdate();
            session.createSQLQuery(
                "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                .executeUpdate();
            session.createSQLQuery("DROP TABLE dogs IF EXISTS").executeUpdate();
            session.createSQLQuery(
                "CREATE TABLE dogs (name varchar(100) primary key, owner varchar(100), CONSTRAINT fk_owner FOREIGN KEY (owner) REFERENCES people(name))")
                .executeUpdate();
            session.createSQLQuery(
                "INSERT INTO dogs VALUES ('Raf', 'Coda')")
                .executeUpdate();
        }

        bootstrap = mock(Bootstrap.class);
        final ObjectMapper objMapper = Jackson.newObjectMapper();
        when(bootstrap.getObjectMapper()).thenReturn(objMapper);
        // Bundle is initialised at start of actual test methods to allow lazy loading to be enabled or disabled.

        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(metricRegistry);
        config.register(new UnitOfWorkApplicationListener("hr-db", sessionFactory));
        config.register(new DogResource(new DogDAO(sessionFactory)));
        config.register(new JacksonMessageBodyProvider(objMapper));
        config.register(new DataExceptionMapper());

        return config;
    }

    @Override
    protected void configureClient(ClientConfig config) {
        config.register(new JacksonMessageBodyProvider(Jackson.newObjectMapper()));
    }

    @Test
    public void serialisesLazyObjectWhenEnabled() throws Exception {
        bundle.initialize(bootstrap);

        final Dog raf = target("/dogs/Raf").request(MediaType.APPLICATION_JSON).get(Dog.class);

        assertThat(raf.getName())
                .isEqualTo("Raf");

        assertThat(raf.getOwner())
                .isNotNull();

        assertThat(raf.getOwner().getName())
                .isEqualTo("Coda");
    }

    @Test
    public void sendsNullWhenDisabled() throws Exception {
        bundle.setLazyLoadingEnabled(false);
        bundle.initialize(bootstrap);

        final Dog raf = target("/dogs/Raf").request(MediaType.APPLICATION_JSON).get(Dog.class);

        assertThat(raf.getName())
                .isEqualTo("Raf");

        assertThat(raf.getOwner())
                .isNull();
    }
}
