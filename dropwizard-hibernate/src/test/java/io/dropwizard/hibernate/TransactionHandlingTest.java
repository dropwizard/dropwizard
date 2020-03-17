package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import io.dropwizard.testing.ResourceHelpers;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;

public class TransactionHandlingTest {

    public static class TestConfiguration extends Configuration {

        DataSourceFactory dataSource = new DataSourceFactory();

        TestConfiguration(@JsonProperty("dataSource") DataSourceFactory dataSource) {
            this.dataSource = dataSource;
        }
    }

    public static class TestApplication extends io.dropwizard.Application<TestConfiguration> {
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
        public void run(TestConfiguration configuration, Environment environment) throws Exception {

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
        Dog create(Dog dog) throws HibernateException {
            currentSession().setHibernateFlushMode(FlushMode.COMMIT);
            currentSession().save(requireNonNull(dog));
            return dog;
        }
    }

    public static class DogResult {
        private Optional<Dog> dog = Optional.empty();

        public Optional<Dog> getDog() {
            return dog;
        }

        public void setDog(Dog dog) {
            this.dog = Optional.ofNullable(dog);
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
                public void setDog(Dog dog) {
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

    private DropwizardTestSupport<?> dropwizardTestSupport = new DropwizardTestSupport<>(
            TestApplication.class,
            ResourceHelpers.resourceFilePath("transaction-handling-test.yaml"),
            ConfigOverride.config("dataSource.url", "jdbc:hsqldb:mem:DbTest" + System.nanoTime() + "?hsqldb.translate_dti_types=false"),
            ConfigOverride.config("server.registerDefaultExceptionMappers", "false")
        );
    private Client client = new JerseyClientBuilder().build();

    @BeforeEach
    public void setup() throws Exception {
        dropwizardTestSupport.before();
    }

    @AfterEach
    public void tearDown() {
        dropwizardTestSupport.after();
        client.close();
    }

    private String getUrlPrefix() {
        return "http://localhost:" + dropwizardTestSupport.getLocalPort();
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
    public void unitOfWorkSupportedNested() throws Exception {
        final Dog raf = client.target(getUrlPrefix() + "/dogs/Raf/nested")
            .request(MediaType.APPLICATION_JSON)
            .get(Dog.class);

        assertThat(raf.getName())
            .isEqualTo("Raf");

        assertThat(raf.getOwner())
            .isNotNull();

        assertThat(requireNonNull(raf.getOwner()).getName())
            .isEqualTo("Coda");
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
    public void unitOfWorkSupportedSerialized() throws Exception {
        final Dog raf = client.target(getUrlPrefix() + "/dogs/Raf/serialized")
            .request(MediaType.APPLICATION_JSON)
            .get(DogResult.class)
            .getDog()
            .get();

        assertThat(raf.getName())
            .isEqualTo("Raf");

        assertThat(raf.getOwner())
            .isNotNull();

        assertThat(requireNonNull(raf.getOwner()).getName())
            .isEqualTo("Coda");
    }
}
