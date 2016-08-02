package io.dropwizard.testing.junit;

import java.util.*;
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
            config.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
            config.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
            config.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
            config.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
            config.setProperty(AvailableSettings.ORDER_UPDATES, "true");
            config.setProperty(AvailableSettings.ORDER_INSERTS, "true");
            config.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
            config.setProperty("jadira.usertype.autoRegisterUserTypes", "true");

            entityClasses.forEach(config::addAnnotatedClass);
            properties.entrySet().forEach(e -> config.setProperty(e.getKey(), e.getValue()));

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

    public <T> T transaction(Supplier<T> supplier) {
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
