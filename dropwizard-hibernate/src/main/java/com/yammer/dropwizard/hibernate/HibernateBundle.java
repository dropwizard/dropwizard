package com.yammer.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.ConfiguredBundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.ConfigurationStrategy;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T> {
    private SessionFactory sessionFactory;

    private final ImmutableList<String> packages;
    private final SessionFactoryFactory sessionFactoryFactory;

    protected HibernateBundle(String... packages) {
        this(ImmutableList.copyOf(packages), new SessionFactoryFactory());
    }

    protected HibernateBundle(ImmutableList<String> packages, SessionFactoryFactory sessionFactoryFactory) {
        this.packages = packages;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapperFactory().registerModule(new Hibernate4Module());
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final DatabaseConfiguration dbConfig = getDatabaseConfiguration(configuration);
        this.sessionFactory = sessionFactoryFactory.build(environment, dbConfig, packages);
        environment.addProvider(new TransactionalResourceMethodDispatchAdapter(sessionFactory));
        environment.addHealthCheck(new SessionFactoryHealthCheck("hibernate",
                                                                 sessionFactory,
                                                                 dbConfig.getValidationQuery()));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
