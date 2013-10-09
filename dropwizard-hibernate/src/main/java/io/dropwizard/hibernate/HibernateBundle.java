package io.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
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
        final DataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities);
        environment.jersey().register(new UnitOfWorkResourceMethodDispatchAdapter(sessionFactory));
        environment.healthChecks().register("hibernate",
                                            new SessionFactoryHealthCheck(sessionFactory,
                                                                          dbConfig.getValidationQuery()));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
