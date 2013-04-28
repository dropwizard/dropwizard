package com.codahale.dropwizard.hibernate;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.ConfiguredBundle;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.db.ConfigurationStrategy;
import com.codahale.dropwizard.db.DatabaseConfiguration;
import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T> {
    private SessionFactory sessionFactory;

    private final ImmutableList<Class<?>> entities;
    private final SessionFactoryFactory sessionFactoryFactory;

    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        this(ImmutableList.<Class<?>>builder().add(entity).add(entities).build(),
             new SessionFactoryFactory());
    }

    protected HibernateBundle(ImmutableList<Class<?>> entities,
                              SessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(new Hibernate4Module());
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final DatabaseConfiguration dbConfig = getDatabaseConfiguration(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities);
        environment.getJerseyEnvironment().addProvider(new UnitOfWorkResourceMethodDispatchAdapter(sessionFactory));
        environment.getAdminEnvironment()
                   .addHealthCheck("hibernate", new SessionFactoryHealthCheck(sessionFactory,
                                                                              dbConfig.getValidationQuery()));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
