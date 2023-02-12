package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Arrays;
import java.util.Optional;

import static io.dropwizard.testing.ConfigOverride.config;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class LazyLoadingTest {
    private final DropwizardAppExtension<TestConfiguration> appExtension = new DropwizardAppExtension<>(
        TestApplication.class, "hibernate-integration-test.yaml", new ResourceConfigurationSourceProvider(),
        config("dataSource.url", "jdbc:h2:mem:DbTest" + System.nanoTime())
    );

    @Test
    void serialisesLazyObjectWhenEnabled() {
        final Dog raf = appExtension.client().target("http://localhost:" + appExtension.getLocalPort()).path("/dogs/Raf").request(MediaType.APPLICATION_JSON).get(Dog.class);

        assertThat(raf.getName()).isEqualTo("Raf");
        assertThat(raf.getOwner()).satisfies(person -> assertThat(person.getName()).isEqualTo("Coda"));
    }

    @Test
    void returnsErrorsWhenEnabled() {
        final Dog raf = new Dog();
        raf.setName("Raf");

        // Raf already exists so this should cause a primary key constraint violation
        final Response response = appExtension.client().target("http://localhost:" + appExtension.getLocalPort()).path("/dogs/Raf").request().put(Entity.entity(raf, MediaType.APPLICATION_JSON));
        assertThat(response.getStatusInfo().toEnum()).isEqualTo(Response.Status.BAD_REQUEST);
        assertThat(response.getHeaderString(HttpHeaders.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(response.readEntity(ErrorMessage.class).getMessage()).contains("Unique index or primary key violation", "PUBLIC.DOGS(NAME)");
    }

    @Nested
    @SuppressWarnings("ClassCanBeStatic")
    @ExtendWith(DropwizardExtensionsSupport.class)
    class LazyLoadingDisabledTest {
        private final DropwizardAppExtension<TestConfiguration> appExtension = new DropwizardAppExtension<>(
            TestApplicationWithDisabledLazyLoading.class, "hibernate-integration-test.yaml",
            new ResourceConfigurationSourceProvider(),
            config("dataSource.url", "jdbc:h2:mem:DbTest" + System.nanoTime())
        );

        @Test
        void sendsNullWhenDisabled() {
            final Dog raf = appExtension.client().target("http://localhost:" + appExtension.getLocalPort()).path("/dogs/Raf").request(MediaType.APPLICATION_JSON).get(Dog.class);

            assertThat(raf.getName()).isEqualTo("Raf");
            assertThat(raf.getOwner()).isNull();
        }
    }

    public static class TestConfiguration extends Configuration {
        DataSourceFactory dataSource;

        TestConfiguration(@JsonProperty("dataSource") DataSourceFactory dataSource) {
            this.dataSource = dataSource;
        }
    }

    public static class TestApplication extends Application<TestConfiguration> {
        final HibernateBundle<TestConfiguration> hibernate = new HibernateBundle<TestConfiguration>(
            Arrays.asList(Person.class, Dog.class), new SessionFactoryFactory()) {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(TestConfiguration configuration) {
                return configuration.dataSource;
            }
        };

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(hibernate);
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) {
            final SessionFactory sessionFactory = hibernate.getSessionFactory();
            initDatabase(sessionFactory);

            environment.jersey().register(new UnitOfWorkApplicationListener("hr-db", sessionFactory));
            environment.jersey().register(new DogResource(new DogDAO(sessionFactory)));
            environment.jersey().register(new PersistenceExceptionMapper());
            environment.jersey().register(new ConstraintViolationExceptionMapper());
        }

        private void initDatabase(SessionFactory sessionFactory) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                session.createNativeQuery(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                    .executeUpdate();
                session.createNativeQuery(
                    "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                    .executeUpdate();
                session.createNativeQuery(
                    "CREATE TABLE dogs (name varchar(100) primary key, owner varchar(100), CONSTRAINT fk_owner FOREIGN KEY (owner) REFERENCES people(name))")
                    .executeUpdate();
                session.createNativeQuery(
                    "INSERT INTO dogs VALUES ('Raf', 'Coda')")
                    .executeUpdate();
                transaction.commit();
            }
        }
    }

    public static class TestApplicationWithDisabledLazyLoading extends TestApplication {
        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            hibernate.setLazyLoadingEnabled(false);
            bootstrap.addBundle(hibernate);
        }
    }

    public static class DogDAO extends AbstractDAO<Dog> {
        DogDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        Optional<Dog> findByName(String name) {
            return Optional.ofNullable(get(name));
        }

        void create(Dog dog) throws HibernateException {
            currentSession().setHibernateFlushMode(FlushMode.COMMIT);
            currentSession().save(dog);
        }
    }

    @Path("/dogs/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public static class DogResource {
        private final DogDAO dao;

        DogResource(DogDAO dao) {
            this.dao = dao;
        }

        @GET
        @UnitOfWork(readOnly = true)
        public Optional<Dog> find(@PathParam("name") String name) {
            return dao.findByName(name);
        }

        @PUT
        @UnitOfWork
        public void create(Dog dog) {
            dao.create(dog);
        }
    }

    public static class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {
        @Override
        public Response toResponse(ConstraintViolationException e) {
            String message = Optional.ofNullable(e.getCause())
                .map(Throwable::getMessage)
                .orElse("");
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(new ErrorMessage(Response.Status.BAD_REQUEST.getStatusCode(), message))
                .build();
        }
    }
}
