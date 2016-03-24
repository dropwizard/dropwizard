package io.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    public static final String DEFAULT_NAME = "hibernate";

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
        bootstrap.getObjectMapper().registerModule(createHibernate5Module());
    }

    /**
     * Override to configure the {@link Hibernate5Module}.
     */
    protected Hibernate5Module createHibernate5Module() {
        return new Hibernate5Module();
    }

    /**
     * Override to configure the name of the bundle
     * (It's used for the bundle health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities, name());
        registerUnitOfWorkListerIfAbsent(environment).registerSessionFactory(name(), sessionFactory);
        environment.healthChecks().register(name(),
                                            new SessionFactoryHealthCheck(
                                                    environment.getHealthCheckExecutorService(),
                                                    dbConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                                    sessionFactory,
                                                    dbConfig.getValidationQuery()));
    }

    private UnitOfWorkApplicationListener registerUnitOfWorkListerIfAbsent(Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof UnitOfWorkApplicationListener) {
                return (UnitOfWorkApplicationListener) singleton;
            }
        }
        final UnitOfWorkApplicationListener listener = new UnitOfWorkApplicationListener();
        environment.jersey().register(listener);
        return listener;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
