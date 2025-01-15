package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class TransactionHandlingTest {
    private final DropwizardAppExtension<TestConfiguration> appExtension = new DropwizardAppExtension<>(
        TestApplication.class,
        "transaction-handling-test.yaml",
        new ResourceConfigurationSourceProvider(),
        ConfigOverride.config("dataSource.url", "jdbc:h2:mem:DbTest" + System.nanoTime()),
        ConfigOverride.config("server.registerDefaultExceptionMappers", "false")
    );

    private String getUrlPrefix() {
        return "http://localhost:" + appExtension.getLocalPort();
    }

    /*
     * This test verifies, that nested transaction with different transactional
     * configuration work.
     *
     * DogDAO#findByName establishes/requires a UnitOfWork and requires a
     * transaction
     *
     * DogResource#findNested establishes/requires a UnitOfWork, but does not
     * require a transaction
     *
     * The test calls into DogResource#findNested, which chains its call to
     * DogDAO#findByName.
     */
    @Test
    void unitOfWorkSupportedNested() {
        final Dog raf = appExtension.client().target(getUrlPrefix()).path("/dogs/Raf/nested")
            .request(MediaType.APPLICATION_JSON)
            .get(Dog.class);

        assertThat(raf.getName()).isEqualTo("Raf");
        assertThat(raf.getOwner()).satisfies(person -> assertThat(person.getName()).isEqualTo("Coda"));
    }

    /*
     * This test verifies, that serialized transactions work.
     *
     * DogResource#findSerialized creates a UnitOfWork, a new instance of a
     * DogResult subclass is returned. After the return the first UnitOfWork
     * ends, the returned instance delegates the loading of the Dog instance
     * to a UnitOfWork wrapped DogDAO#findByName. This is the second transaction
     * and both transaction need to work to satisfy this test.
     */
    @Test
    void unitOfWorkSupportedSerialized() {
        Optional<Dog> raf = appExtension.client().target(getUrlPrefix()).path("/dogs/Raf/serialized")
            .request(MediaType.APPLICATION_JSON)
            .get(DogResult.class)
            .getDog();

        assertThat(raf).hasValueSatisfying(dog -> {
                assertThat(dog.getName()).isEqualTo("Raf");
                assertThat(dog.getOwner()).satisfies(person -> assertThat(person.getName()).isEqualTo("Coda"));
            }
        );
    }

    public static class TestConfiguration extends Configuration {
        final DataSourceFactory dataSource;

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

            UnitOfWorkAwareProxyFactory unitOfWorkProxyFactory = new UnitOfWorkAwareProxyFactory(hibernate);
            DogDAO dao = unitOfWorkProxyFactory.create(DogDAO.class, SessionFactory.class, sessionFactory);

            environment.jersey().register(new UnitOfWorkApplicationListener("hr-db", sessionFactory));
            environment.jersey().register(new DogResource(dao));
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

    public static class DogDAO extends AbstractDAO<Dog> {
        DogDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        @UnitOfWork
        Optional<Dog> findByName(String name) {
            return Optional.ofNullable(get(name));
        }

        @UnitOfWork
        void create(Dog dog) throws HibernateException {
            currentSession().setHibernateFlushMode(FlushMode.COMMIT);
            currentSession().save(dog);
        }
    }

    public static class DogResult {
        @Nullable
        private Dog dog;

        public Optional<Dog> getDog() {
            return Optional.ofNullable(dog);
        }

        public void setDog(@Nullable Dog dog) {
            this.dog = dog;
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
        @Path("serialized")
        @UnitOfWork(transactional = false)
        public DogResult findSerialized(@PathParam("name") String name) {
            return new DogResult() {
                @Override
                public void setDog(@Nullable Dog dog) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Optional<Dog> getDog() {
                    return dao.findByName(name);
                }
            };
        }

        @GET
        @Path("nested")
        @UnitOfWork(transactional = false)
        public Optional<Dog> findNested(@PathParam("name") String name) {
            return dao.findByName(name);
        }

        @PUT
        @UnitOfWork
        public void create(Dog dog) {
            dao.create(dog);
        }
    }
}
