package io.dropwizard.testing.junit;

import io.dropwizard.testing.common.DAOTest;
import org.hibernate.SessionFactory;
import org.junit.rules.ExternalResource;

import java.util.concurrent.Callable;

//@formatter:off
/**
 * A JUnit rule for testing DAOs and Hibernate entities. It allows to quickly
 * test the database access code without starting the Dropwizard infrastructure.
 * <p>
 * Example:
 * <pre><code>
 * {@literal @}Rule
    public DAOTestRule daoTestRule = DAOTestRule.newBuilder()
          .addEntityClass(Person.class)
          .build();

    private PersonDAO personDAO;

   {@literal @}Before
    public void setUp() throws Exception {
        personDAO = new PersonDAO(daoTestRule.getSessionFactory());
    }

   {@literal @}Test
    public void createPerson() {
        Person wizard = daoTestRule.inTransaction(() -> personDAO.create(new Person("Merlin", "The chief wizard")));
        assertThat(wizard.getId()).isGreaterThan(0);
        assertThat(wizard.getFullName()).isEqualTo("Merlin");
        assertThat(wizard.getJobTitle()).isEqualTo("The chief wizard");
    }
 * </code></pre>
 * </p>
 *
 * @deprecated Deprecated since Dropwizard 2.0.0. Please migrate to JUnit 5 and {@link io.dropwizard.testing.junit5.DAOTestExtension}.
 */
//@formatter:on
@Deprecated
public class DAOTestRule extends ExternalResource {
    private final DAOTest daoTest;

    public static class Builder extends DAOTest.Builder<Builder> {
        public DAOTestRule build() {
            return new DAOTestRule(buildDAOTest());
        }
    }

    /**
     * Creates a new builder for {@link DAOTestRule}, which allows to customize a {@link SessionFactory}
     * by different parameters. By default uses the H2 database in the memory mode.
     *
     * @return a new {@link Builder}
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Use {@link DAOTestRule#newBuilder()}
     */
    private DAOTestRule(DAOTest daoTest) {
        this.daoTest = daoTest;
    }

    @Override
    protected void before() throws Throwable {
        daoTest.before();
    }

    @Override
    protected void after() {
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
