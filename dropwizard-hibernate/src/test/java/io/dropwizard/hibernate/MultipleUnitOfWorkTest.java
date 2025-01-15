package io.dropwizard.hibernate;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.ResourceHelpers;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.jspecify.annotations.Nullable;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(DropwizardExtensionsSupport.class)
class MultipleUnitOfWorkTest {
    private static final DropwizardAppExtension<TestConfiguration> appExtension = new DropwizardAppExtension<>(
        TestApplication.class,
        ResourceHelpers.resourceFilePath("hibernate-multiple-unitofwork.yaml"),
        ConfigOverride.config("dataSource.url", "jdbc:h2:mem:multiple-unitofwork-dog-" + System.nanoTime()),
        ConfigOverride.config("personDataSource.url", "jdbc:h2:mem:multiple-unitofwork-person-" + System.nanoTime())
    );

    @Nullable
    private static TestResource staticTestResource;

    private String baseUri() {
        return "http://localhost:" + appExtension.getLocalPort();
    }

    public Boolean request(String path) {
        return appExtension.client().target(baseUri()).path(path)
            .request(MediaType.TEXT_PLAIN)
            .get(Boolean.class);
    }

    @Test
    void testDog() {
        assertThat(request("/test/dog")).isTrue();
        assertThat(requireNonNull(staticTestResource).getDog()).isTrue();
    }

    @Test
    void testPerson() {
        assertThat(request("/test/person")).isTrue();
        assertThat(requireNonNull(staticTestResource).getPerson()).isTrue();
    }

    @Test
    void testFailingDog() {
        assertThatThrownBy(() -> request("/test/failingDog")).isInstanceOf(InternalServerErrorException.class);
        assertThatThrownBy(() -> requireNonNull(staticTestResource).getFailingDog()).isInstanceOf(HibernateException.class);
    }

    @Test
    void testDogMultiple() {
        assertThat(request("/test/dogMultiple")).isTrue();
        assertThat(requireNonNull(staticTestResource).getDogMultiple()).isTrue();
    }

    @Test
    void testFailingPerson() {
        assertThatThrownBy(() -> request("/test/failingPerson")).isInstanceOf(InternalServerErrorException.class);
        assertThatThrownBy(() -> requireNonNull(staticTestResource).getFailingPerson()).isInstanceOf(HibernateException.class);
    }

    @Test
    void testDogAndPerson() {
        assertThat(request("/test/dogAndPerson")).isTrue();
        assertThat(requireNonNull(staticTestResource).getDogAndPerson()).isTrue();
    }

    @Test
    void testDeclaredAndHandlingUnitOfWork() {
        assertThat(request("/test/definitionAndHandlingUnitOfWork")).isTrue();
    }

    public static class TestConfiguration extends Configuration {
        final DataSourceFactory dataSource;
        final DataSourceFactory personDataSource;

        TestConfiguration(@JsonProperty("dataSource") DataSourceFactory dataSource,
                          @JsonProperty("personDataSource") DataSourceFactory personDataSource) {
            this.dataSource = dataSource;
            this.personDataSource = personDataSource;
        }
    }

    public static class TestApplication extends Application<TestConfiguration> {
        final HibernateBundle<TestConfiguration> dogHibernate = new HibernateBundle<TestConfiguration>(Dog.class, Person.class) {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(TestConfiguration configuration) {
                return configuration.dataSource;
            }

            @Override
            public String name() {
                return "hibernate.dog";
            }
        };

        final HibernateBundle<TestConfiguration> personHibernate = new HibernateBundle<TestConfiguration>(Person.class) {
            @Override
            public PooledDataSourceFactory getDataSourceFactory(TestConfiguration configuration) {
                return configuration.personDataSource;
            }

            @Override
            public String name() {
                return "hibernate.person";
            }
        };

        @Override
        public void initialize(Bootstrap<TestConfiguration> bootstrap) {
            bootstrap.addBundle(dogHibernate);
            bootstrap.addBundle(personHibernate);
        }

        @Override
        public void run(TestConfiguration configuration, Environment environment) throws Exception {
            final SessionFactory dogSessionFactory = dogHibernate.getSessionFactory();
            final SessionFactory personSessionFactory = personHibernate.getSessionFactory();
            initDatabase(dogSessionFactory, personSessionFactory);

            UnitOfWorkAwareProxyFactory proxyFactory = new UnitOfWorkAwareProxyFactory(dogHibernate, personHibernate);
            staticTestResource = proxyFactory.create(TestResource.class,
                new Class[]{SessionFactory.class, SessionFactory.class}, new Object[] {dogSessionFactory, personSessionFactory});

            environment.jersey().register(new TestResource(dogSessionFactory, personSessionFactory));
        }

        private void initDatabase(SessionFactory dogSessionFactory, SessionFactory personSessionFactory) {
            try (Session dogSession = dogSessionFactory.openSession();
                 Session personSession = personSessionFactory.openSession()) {
                Transaction personTransaction = personSession.beginTransaction();
                personSession.createNativeQuery(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                    .executeUpdate();
                personSession.createNativeQuery(
                    "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                    .executeUpdate();
                personTransaction.commit();
                Transaction dogTransaction = dogSession.beginTransaction();
                dogSession.createNativeQuery(
                    "CREATE TABLE people (name varchar(100) primary key, email varchar(16), birthday timestamp with time zone)")
                    .executeUpdate();
                dogSession.createNativeQuery(
                    "INSERT INTO people VALUES ('Coda', 'coda@example.com', '1979-01-02 00:22:00+0:00')")
                    .executeUpdate();
                dogSession.createNativeQuery(
                    "CREATE TABLE dogs (name varchar(100) primary key, owner varchar(100), CONSTRAINT fk_owner FOREIGN KEY (owner) REFERENCES people(name))")
                    .executeUpdate();
                dogSession.createNativeQuery(
                    "INSERT INTO dogs VALUES ('Raf', 'Coda')")
                    .executeUpdate();
                dogTransaction.commit();
            }
        }
    }

    public static abstract class AbstractTestResource {
        @GET
        @Path("/definitionAndHandlingUnitOfWork")
        @UnitOfWork(value = "hibernate.dog")
        public abstract boolean getDefinitionAndHandlingUnitOfWork();
    }

    @Path("/test")
    public static class TestResource extends AbstractTestResource {
        private final SessionFactory dogSessionFactory;
        private final SessionFactory personSessionFactory;

        public TestResource(SessionFactory dogSessionFactory, SessionFactory personSessionFactory) {
            this.dogSessionFactory = dogSessionFactory;
            this.personSessionFactory = personSessionFactory;
        }

        @GET
        @Path("/dog")
        @UnitOfWork(value = "hibernate.dog")
        public boolean getDog() {
            return dogSessionFactory.getCurrentSession() != null
                && dogSessionFactory.getCurrentSession().isOpen();
        }

        @GET
        @Path("/person")
        @UnitOfWork(value = "hibernate.person")
        public boolean getPerson() {
            return personSessionFactory.getCurrentSession() != null
                && personSessionFactory.getCurrentSession().isOpen();
        }

        @GET
        @Path("/failingDog")
        @UnitOfWork(value = "hibernate.person")
        public boolean getFailingDog() {
            return dogSessionFactory.getCurrentSession() != null
                && dogSessionFactory.getCurrentSession().isOpen();
        }

        @GET
        @Path("/dogMultiple")
        @UnitOfWork(value = "hibernate.dog")
        @UnitOfWork(value = "hibernate.dog", readOnly = true)
        public boolean getDogMultiple() {
            return dogSessionFactory.getCurrentSession() != null
                && dogSessionFactory.getCurrentSession().isOpen()
                && dogSessionFactory.getCurrentSession().isDefaultReadOnly();
        }

        @GET
        @Path("/failingPerson")
        @UnitOfWork(value = "hibernate.dog")
        public boolean getFailingPerson() {
            return personSessionFactory.getCurrentSession() != null
                && personSessionFactory.getCurrentSession().isOpen();
        }

        @GET
        @Path("/dogAndPerson")
        @UnitOfWork(value = "hibernate.dog")
        @UnitOfWork(value = "hibernate.person")
        public boolean getDogAndPerson() {
            return dogSessionFactory.getCurrentSession() != null
                && personSessionFactory.getCurrentSession() != null
                && dogSessionFactory.getCurrentSession().isOpen()
                && personSessionFactory.getCurrentSession().isOpen();
        }

        @UnitOfWork(value = "hibernate.person")
        @Override
        public boolean getDefinitionAndHandlingUnitOfWork() {
            return dogSessionFactory.getCurrentSession() != null
                && personSessionFactory.getCurrentSession() != null
                && dogSessionFactory.getCurrentSession().isOpen()
                && personSessionFactory.getCurrentSession().isOpen();
        }
    }
}
