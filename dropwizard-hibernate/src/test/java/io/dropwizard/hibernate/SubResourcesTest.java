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
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientProperties;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class SubResourcesTest {
    private static final DropwizardAppExtension<TestConfiguration> appExtension = new DropwizardAppExtension<>(
        TestApplication.class,
        "hibernate-sub-resource-test.yaml",
        new ResourceConfigurationSourceProvider(),
        ConfigOverride.config("dataSource.url", "jdbc:h2:mem:sub-resources-" + System.nanoTime()));

    private String baseUri() {
        return "http://localhost:" + appExtension.getLocalPort();
    }

    @Test
    void canReadFromTopResource() {
        final Person person = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people/Greg")
            .request()
            .get(Person.class);

        assertThat(person.getName()).isEqualTo("Greg");
    }

    @Test
    void canWriteTopResource() {
        final Person person = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people")
            .request()
            .post(Entity.entity("{\"name\": \"Jason\", \"email\": \"jason@gmail.com\", \"birthday\":637317407000}",
                MediaType.APPLICATION_JSON_TYPE), Person.class);

        assertThat(person.getName()).isEqualTo("Jason");
    }

    @Test
    void canReadFromSubResources() {
        final Dog dog = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people/Greg/dogs/Bello")
            .request()
            .get(Dog.class);

        assertThat(dog.getName()).isEqualTo("Bello");
        assertThat(dog.getOwner()).isNotNull();
        assertThat(requireNonNull(dog.getOwner()).getName()).isEqualTo("Greg");
    }

    @Test
    void canWriteSubResource() {
        final Dog dog = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people/Greg/dogs")
            .request()
            .post(Entity.entity("{\"name\": \"Bandit\"}", MediaType.APPLICATION_JSON_TYPE), Dog.class);

        assertThat(dog.getName()).isEqualTo("Bandit");
        assertThat(dog.getOwner()).isNotNull();
        assertThat(requireNonNull(dog.getOwner()).getName()).isEqualTo("Greg");
    }

    @Test
    void errorsAreHandled() {
        Response response = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people/Jim/dogs")
            .request()
            .post(Entity.entity("{\"name\": \"Bullet\"}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    void noSessionErrorIsRaised() {
        Response response = appExtension.client()
            .property(ClientProperties.CONNECT_TIMEOUT, 0)
            .target(baseUri())
            .path("/people/Greg/dogs")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(500);
    }

    public static class TestConfiguration extends Configuration {
        final DataSourceFactory dataSource;

        TestConfiguration(@JsonProperty("dataSource") DataSourceFactory dataSource) {
            this.dataSource = dataSource;
        }
    }

    public static class TestApplication extends Application<TestConfiguration> {
        final HibernateBundle<TestConfiguration> hibernate = new HibernateBundle<>(Person.class, Dog.class) {
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
            environment.jersey().register(new PersonResource(new PersonDAO(sessionFactory), new DogDAO(sessionFactory)));
        }

        private void initDatabase(SessionFactory sessionFactory) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                session.createNativeQuery("CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp)")
                    .executeUpdate();
                session.createNativeQuery("INSERT INTO people VALUES ('Greg', 'greg@yahooo.com', '1989-02-13')")
                    .executeUpdate();
                session.createNativeQuery("CREATE TABLE dogs (name varchar(100) primary key, owner varchar(100) REFERENCES people(name))")
                    .executeUpdate();
                session.createNativeQuery("INSERT INTO dogs VALUES ('Bello', 'Greg')")
                    .executeUpdate();
                transaction.commit();
            }
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Path("/people")
    public static class PersonResource {
        private final PersonDAO personDao;
        private final DogResource dogResource;

        PersonResource(PersonDAO dao, DogDAO dogDao) {
            this.personDao = dao;
            this.dogResource = new DogResource(dogDao, personDao);
        }

        @GET
        @Path("{name}")
        @UnitOfWork(readOnly = true)
        public Optional<Person> find(@PathParam("name") String name) {
            return personDao.findByName(name);
        }

        @POST
        @UnitOfWork
        public Person save(Person person) {
            return personDao.persist(person);
        }

        @Path("/{ownerName}/dogs")
        public DogResource dogResource() {
            return dogResource;
        }
    }

    @Produces(MediaType.APPLICATION_JSON)
    public static class DogResource {

        private final DogDAO dogDAO;
        private final PersonDAO personDAO;

        DogResource(DogDAO dogDAO, PersonDAO personDAO) {
            this.dogDAO = dogDAO;
            this.personDAO = personDAO;
        }

        @GET
        // Intentionally no `@UnitOfWork`
        public List<Dog> findAll(@PathParam("ownerName") String ownerName) {
            return dogDAO.findByOwner(ownerName);
        }

        @GET
        @Path("{dogName}")
        @UnitOfWork(readOnly = true)
        public Optional<Dog> find(@PathParam("ownerName") String ownerName,
                                  @PathParam("dogName") String dogName) {
            return dogDAO.findByOwnerAndName(ownerName, dogName);
        }

        @POST
        @UnitOfWork
        public Dog create(@PathParam("ownerName") String ownerName, Dog dog) {
            dog.setOwner(personDAO.findByName(ownerName).orElseThrow(() -> new WebApplicationException(404)));
            return dogDAO.persist(dog);
        }
    }

    public static class PersonDAO extends AbstractDAO<Person> {
        PersonDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        Optional<Person> findByName(String name) {
            return Optional.ofNullable(get(name));
        }
    }

    public static class DogDAO extends AbstractDAO<Dog> {
        DogDAO(SessionFactory sessionFactory) {
            super(sessionFactory);
        }

        Optional<Dog> findByOwnerAndName(String ownerName, String dogName) {
            return query("SELECT d FROM Dog d WHERE d.owner.name=:owner AND d.name=:name")
                .setParameter("owner", ownerName)
                .setParameter("name", dogName)
                .uniqueResultOptional();
        }

        List<Dog> findByOwner(String ownerName) {
            return query("SELECT d FROM Dog d WHERE d.owner.name=:owner")
                .setParameter("owner", ownerName)
                .list();
        }
    }
}
