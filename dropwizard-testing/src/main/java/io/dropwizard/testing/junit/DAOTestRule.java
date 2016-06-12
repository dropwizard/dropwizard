package io.dropwizard.testing.junit;

import java.util.function.Supplier;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
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

        public Builder setCurrentSessionContextClass(final String currentSessionContextClass) {
            hibernateConfiguration.currentSessionContextClass = currentSessionContextClass;
            return this;
        }

        public Builder setHbm2DdlAuto(final String hbm2ddlAuto) {
            hibernateConfiguration.hbm2ddlAuto = hbm2ddlAuto;
            return this;
        }

        public Builder setShowSql(final boolean showSql) {
            hibernateConfiguration.showSql = Boolean.toString(showSql);
            return this;
        }

        public Builder addEntityClass(final Class<?> entityClass) {
            hibernateConfiguration.entityClasses.add(entityClass);
            return this;
        }

        public DAOTestRule build() {
            final Configuration config = new Configuration();
            config.setProperty("hibernate.connection.url", hibernateConfiguration.connectionUrl);
            config.setProperty("hibernate.connection.username", hibernateConfiguration.connectionUsername);
            config.setProperty("hibernate.connection.driver_class", hibernateConfiguration.connectionDriverClass);
            config.setProperty("hibernate.current_session_context_class", hibernateConfiguration.currentSessionContextClass);
            config.setProperty("hibernate.hbm2ddl.auto", hibernateConfiguration.hbm2ddlAuto);
            config.setProperty("hibernate.show_sql", hibernateConfiguration.showSql);

            for (Class<?> entityClass : hibernateConfiguration.entityClasses) {
                config.addAnnotatedClass(entityClass);
            }

            final SessionFactory sessionFactory = config.buildSessionFactory();

            return new DAOTestRule(sessionFactory);
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
