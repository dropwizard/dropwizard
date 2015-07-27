package io.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate4.Hibernate4Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.AbstractConfiguredHttpBundle;
import io.dropwizard.ConfiguredHttpBundle;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.HttpEnvironment;
import io.dropwizard.util.Duration;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends HttpConfiguration>
        extends AbstractConfiguredHttpBundle<T>
        implements ConfiguredHttpBundle<T>, DatabaseConfiguration<T> {
    private static final String DEFAULT_NAME = "hibernate";

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
        bootstrap.getObjectMapper().registerModule(createHibernate4Module());
    }

    /**
     * Override to configure the {@link Hibernate4Module}.
     */
    protected Hibernate4Module createHibernate4Module() {
        return new Hibernate4Module();
    }

    /**
     * Override to configure the name of the bundle
     * (It's used for the bundle health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    @Override
    public final void run(T configuration, HttpEnvironment environment) throws Exception {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities, name());
        environment.jersey().register(new UnitOfWorkApplicationListener(sessionFactory));
        environment.healthChecks().register(name(),
                                            new SessionFactoryHealthCheck(
                                                    environment.getHealthCheckExecutorService(),
                                                    dbConfig.getHealthCheckValidationTimeout().or(Duration.seconds(5)),
                                                    sessionFactory,
                                                    dbConfig.getHealthCheckValidationQuery()));
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
