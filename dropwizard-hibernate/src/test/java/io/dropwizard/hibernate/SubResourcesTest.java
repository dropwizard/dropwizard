package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;

public class SubResourcesTest {

    public static class TestConfiguration extends Configuration {

        DataSourceFactory dataSource = new DataSourceFactory();

        TestConfiguration(@JsonProperty("dataSource") DataSourceFactory dataSource) {
            this.dataSource = dataSource;
        }
    }

    public static class TestApplication extends Application<TestConfiguration> {
        final HibernateBundle<TestConfiguration> hibernate = new HibernateBundle<TestConfiguration>(Person.class, Dog.class) {
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
            Optional<Person> person = personDAO.findByName(ownerName);
            if (!person.isPresent()) {
                throw new WebApplicationException(404);
            }
            dog.setOwner(person.get());
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

    @ClassRule
    public static DropwizardAppRule<TestConfiguration> appRule = new DropwizardAppRule<>(TestApplication.class,
        ResourceHelpers.resourceFilePath("hibernate-sub-resource-test.yaml"));

    private static String baseUri() {
        return "http://localhost:" + appRule.getLocalPort();
    }

    @Test
    public void canReadFromTopResource() throws Exception {
        final Person person = appRule.client()
            .target(baseUri() + "/people/Greg")
            .request()
            .get(Person.class);

        assertThat(person.getName()).isEqualTo("Greg");
    }

    @Test
    public void canWriteTopResource() throws Exception {
        final Person person = appRule.client()
            .target(baseUri() + "/people")
            .request()
            .post(Entity.entity("{\"name\": \"Jason\", \"email\": \"jason@gmail.com\", \"birthday\":637317407000}",
                MediaType.APPLICATION_JSON_TYPE), Person.class);

        assertThat(person.getName()).isEqualTo("Jason");
    }

    @Test
    public void canReadFromSubResources() throws Exception {
        final Dog dog = appRule.client()
            .target(baseUri() + "/people/Greg/dogs/Bello")
            .request()
            .get(Dog.class);

        assertThat(dog.getName()).isEqualTo("Bello");
        assertThat(dog.getOwner()).isNotNull();
        assertThat(requireNonNull(dog.getOwner()).getName()).isEqualTo("Greg");
    }

    @Test
    public void canWriteSubResource() throws Exception {
        final Dog dog = appRule.client()
            .target(baseUri() + "/people/Greg/dogs")
            .request()
            .post(Entity.entity("{\"name\": \"Bandit\"}", MediaType.APPLICATION_JSON_TYPE), Dog.class);

        assertThat(dog.getName()).isEqualTo("Bandit");
        assertThat(dog.getOwner()).isNotNull();
        assertThat(requireNonNull(dog.getOwner()).getName()).isEqualTo("Greg");
    }

    @Test
    public void errorsAreHandled() throws Exception {
        Response response = appRule.client()
            .target(baseUri() + "/people/Jim/dogs")
            .request()
            .post(Entity.entity("{\"name\": \"Bullet\"}", MediaType.APPLICATION_JSON_TYPE));
        assertThat(response.getStatus()).isEqualTo(404);
    }

    @Test
    public void noSessionErrorIsRaised() throws Exception {
        Response response = appRule.client()
            .target(baseUri() + "/people/Greg/dogs")
            .request()
            .get();
        assertThat(response.getStatus()).isEqualTo(500);
    }

}
