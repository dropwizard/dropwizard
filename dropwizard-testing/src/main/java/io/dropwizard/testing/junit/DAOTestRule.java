package io.dropwizard.testing.junit;

import java.util.function.Supplier;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.context.internal.ManagedSessionContext;
import org.junit.rules.ExternalResource;

import io.dropwizard.logging.BootstrapLogging;

/**
 * A JUnit {@link ExternalResource} for testing DAOs and Hibernate entities.
 */
public class DAOTestRule extends ExternalResource {

    static {
        BootstrapLogging.bootstrap();
    }

    public static class Builder {

        private DAOTestHibernateConfiguration hibernateConfiguration = new DAOTestHibernateConfiguration();

        public Builder setConnectionUrl(final String connectionUrl) {
            hibernateConfiguration.connectionUrl = connectionUrl;
            return this;
        }

        public Builder setConnectionUsername(final String connectionUsername) {
            hibernateConfiguration.connectionUsername = connectionUsername;
            return this;
        }

        public Builder setConnectionDriverClass(final Class<? extends java.sql.Driver> driverClass) {
            hibernateConfiguration.connectionDriverClass = driverClass.getName();
            return this;
        }

        public Builder setHbm2DdlAuto(final String hbm2ddlAuto) {
            hibernateConfiguration.hbm2ddlAuto = hbm2ddlAuto;
            return this;
        }

        public Builder setShowSql(final boolean showSql) {
            hibernateConfiguration.showSql = showSql;
            return this;
        }

        public Builder useSqlComments(final boolean useSqlComments) {
            hibernateConfiguration.useSqlComments = useSqlComments;
            return this;
        }

        public Builder addEntityClass(final Class<?> entityClass) {
            hibernateConfiguration.entityClasses.add(entityClass);
            return this;
        }

        public Builder setProperty(String key, String value) {
            hibernateConfiguration.properties.put(key, value);
            return this;
        }

        public DAOTestRule build() {
            final Configuration config = new Configuration();
            config.setProperty(AvailableSettings.URL, hibernateConfiguration.connectionUrl);
            config.setProperty(AvailableSettings.USER, hibernateConfiguration.connectionUsername);
            config.setProperty(AvailableSettings.PASS, hibernateConfiguration.connectionPassword);
            config.setProperty(AvailableSettings.DRIVER, hibernateConfiguration.connectionDriverClass);
            config.setProperty(AvailableSettings.HBM2DDL_AUTO, hibernateConfiguration.hbm2ddlAuto);
            config.setProperty(AvailableSettings.SHOW_SQL, String.valueOf(hibernateConfiguration.showSql));
            config.setProperty(AvailableSettings.USE_SQL_COMMENTS,
                String.valueOf(hibernateConfiguration.useSqlComments));
            config.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
            config.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
            config.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
            config.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
            config.setProperty(AvailableSettings.ORDER_UPDATES, "true");
            config.setProperty(AvailableSettings.ORDER_INSERTS, "true");
            config.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
            config.setProperty("jadira.usertype.autoRegisterUserTypes", "true");

            hibernateConfiguration.entityClasses.forEach(config::addAnnotatedClass);
            hibernateConfiguration.properties.entrySet().forEach(e -> config.setProperty(e.getKey(), e.getValue()));

            return new DAOTestRule(config.buildSessionFactory());
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final SessionFactory sessionFactory;

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

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public <T> T transaction(final Supplier<T> supplier) {
        final Session session = sessionFactory.getCurrentSession();
        final Transaction transaction = session.beginTransaction();
        try {
            final T result = supplier.get();
            transaction.commit();
            return result;
        } catch (final RuntimeException e) {
            transaction.rollback();
            throw e;
        }
    }
}
