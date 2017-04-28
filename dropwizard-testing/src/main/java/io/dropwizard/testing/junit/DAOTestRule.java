package io.dropwizard.testing.junit;

import com.google.common.base.Throwables;
import io.dropwizard.logging.BootstrapLogging;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.rules.ExternalResource;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

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
 */
public class DAOTestRule extends ExternalResource {

    static {
        BootstrapLogging.bootstrap();
    }

    public static class Builder {

        private String url = "jdbc:h2:mem:" + UUID.randomUUID();
        private String username = "sa";
        private String password = "";
        private String driver = "org.h2.Driver";
        private String hbm2ddlAuto = "create";
        private boolean showSql = false;
        private boolean useSqlComments = false;
        private Set<Class<?>> entityClasses = new LinkedHashSet<>();
        private Map<String, String> properties = new HashMap<>();

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setDriver(Class<? extends java.sql.Driver> driver) {
            this.driver = driver.getName();
            return this;
        }

        public Builder setHbm2DdlAuto(String hbm2ddlAuto) {
            this.hbm2ddlAuto = hbm2ddlAuto;
            return this;
        }

        public Builder setShowSql(boolean showSql) {
            this.showSql = showSql;
            return this;
        }

        public Builder useSqlComments(boolean useSqlComments) {
            this.useSqlComments = useSqlComments;
            return this;
        }

        public Builder addEntityClass(Class<?> entityClass) {
            this.entityClasses.add(entityClass);
            return this;
        }

        public Builder setProperty(String key, String value) {
            this.properties.put(key, value);
            return this;
        }

        public DAOTestRule build() {
            final Configuration config = new Configuration();
            config.setProperty(AvailableSettings.URL, url);
            config.setProperty(AvailableSettings.USER, username);
            config.setProperty(AvailableSettings.PASS, password);
            config.setProperty(AvailableSettings.DRIVER, driver);
            config.setProperty(AvailableSettings.HBM2DDL_AUTO, hbm2ddlAuto);
            config.setProperty(AvailableSettings.SHOW_SQL, String.valueOf(showSql));
            config.setProperty(AvailableSettings.USE_SQL_COMMENTS, String.valueOf(useSqlComments));
            // Use the same configuration as in the Hibernate bundle to reduce differences between
            // testing and production environments.
            config.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
            config.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
            config.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
            config.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
            config.setProperty(AvailableSettings.ORDER_UPDATES, "true");
            config.setProperty(AvailableSettings.ORDER_INSERTS, "true");
            config.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
            config.setProperty("jadira.usertype.autoRegisterUserTypes", "true");

            entityClasses.forEach(config::addAnnotatedClass);
            properties.forEach(config::setProperty);

            return new DAOTestRule(config.buildSessionFactory());
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

    private final SessionFactory sessionFactory;

    /**
     * Use {@link DAOTestRule#newBuilder()}
     */
    private DAOTestRule(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    protected void before() throws Throwable {
        if (ManagedSessionContext.hasBind(sessionFactory)) {
            return;
        }

        final Session session = sessionFactory.openSession();
        ManagedSessionContext.bind(session);
    }

    @Override
    protected void after() {
        if (!ManagedSessionContext.hasBind(sessionFactory)) {
            return;
        }

        final Session currentSession = sessionFactory.getCurrentSession();
        if (currentSession.isOpen()) {
            currentSession.close();
        }
        ManagedSessionContext.unbind(sessionFactory);
    }

    /**
     * Returns the current active session factory for injecting to DAOs.
     *
     * @return {@link SessionFactory} with an open session.
     */
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Performs a call in a transaction
     *
     * @param call the call
     * @param <T>  the type of the returned result
     * @return the result of the call
     */
    public <T> T inTransaction(Callable<T> call) {
        final Session session = sessionFactory.getCurrentSession();
        final Transaction transaction = session.beginTransaction();
        try {
            final T result = call.call();
            transaction.commit();
            return result;
        } catch (final Exception e) {
            transaction.rollback();
            Throwables.throwIfUnchecked(e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Performs an action in a transaction
     *
     * @param action the action
     */
    public void inTransaction(Runnable action) {
        inTransaction(() -> {
            action.run();
            return true;
        });
    }
}
