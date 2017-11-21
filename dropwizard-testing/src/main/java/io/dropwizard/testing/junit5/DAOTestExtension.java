package io.dropwizard.testing.junit5;

import io.dropwizard.testing.common.DAOTest;
import io.dropwizard.testing.junit.DAOTestRule;
import org.hibernate.SessionFactory;

import java.util.concurrent.Callable;

//@formatter:off
/**
 * An extension for testing DAOs and Hibernate entities. It allows to quickly
 * test the database access code without starting the Dropwizard infrastructure.
 * <p>
 * Example:
 * <pre><code>
    public DAOTestExtension daoTestExtension = DAOTestExtension.newBuilder()
          .addEntityClass(Person.class)
          .build();

    private PersonDAO personDAO;

   {@literal @}BeforeEach
    public void setUp() throws Exception {
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
    }

   {@literal @}Test
    public void createPerson() {
        Person wizard = daoTestExtension.inTransaction(() -> personDAO.create(new Person("Merlin", "The chief wizard")));
        assertThat(wizard.getId()).isGreaterThan(0);
        assertThat(wizard.getFullName()).isEqualTo("Merlin");
        assertThat(wizard.getJobTitle()).isEqualTo("The chief wizard");
    }
 * </code></pre>
 * </p>
 */
//@formatter:on
public class DAOTestExtension implements DropwizardExtension {
    private final DAOTest daoTest;

    public static class Builder extends DAOTest.Builder<Builder> {
        public DAOTestExtension build() {
            return new DAOTestExtension(buildDAOTest());
        }
    }

    /**
     * Creates a new builder for {@link DAOTestRule}, which allows to customize a {@link SessionFactory}
     * by different parameters. By default uses the H2 database in the memory mode.
     *
     * @return a new {@link DAOTestRule.Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Use {@link DAOTestRule#newBuilder()}
     */
    private DAOTestExtension(DAOTest daoTest) {
        this.daoTest = daoTest;
    }

    @Override
    public void before() throws Throwable {
        daoTest.before();
    }

    @Override
    public void after() {
        daoTest.after();
    }

    /**
     * Returns the current active session factory for injecting to DAOs.
     *
     * @return {@link SessionFactory} with an open session.
     */
    public SessionFactory getSessionFactory() {
        return daoTest.getSessionFactory();
    }

    /**
     * Performs a call in a transaction
     *
     * @param call the call
     * @param <T>  the type of the returned result
     * @return the result of the call
     */
    public <T> T inTransaction(Callable<T> call) {
        return daoTest.inTransaction(call);
    }

    /**
     * Performs an action in a transaction
     *
     * @param action the action
     */
    public void inTransaction(Runnable action) {
        daoTest.inTransaction(action);
    }
}
