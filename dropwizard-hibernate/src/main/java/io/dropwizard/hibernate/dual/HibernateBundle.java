package io.dropwizard.hibernate.dual;

import static java.util.Objects.requireNonNull;

import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.hibernate.SessionFactoryHealthCheck;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.hibernate.SessionFactory;

import java.util.List;

public abstract class HibernateBundle<T> extends io.dropwizard.hibernate.HibernateBundle<T>
{
    public static final String PRIMARY = "hibernate-primary";
    public static final String READER = "hibernate-reader";

    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        super(entity, entities);
    }

    protected HibernateBundle(List<Class<?>> entities,
                              SessionFactoryFactory sessionFactoryFactory) {
        super(entities, sessionFactoryFactory);
    }

    @Override
    protected String name() {
        return PRIMARY;
    }

    protected String reader() {
        return READER;
    }

    abstract public PooledDataSourceFactory getReadSourceFactory(T configuration);

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final PooledDataSourceFactory primaryConfig = getDataSourceFactory(configuration);
        final SessionFactory primary = requireNonNull(sessionFactoryFactory.build(this, environment, primaryConfig,
            entities, name()));
        final PooledDataSourceFactory readerConfig = getReadSourceFactory(configuration);
        final SessionFactory reader = requireNonNull(sessionFactoryFactory.build(this, environment, readerConfig,
            entities, reader()));
        final DualSessionFactory factory = new DualSessionFactory(primary, reader);
        registerUnitOfWorkListenerIfAbsent(environment).registerSessionFactory(name(), factory);
        environment.healthChecks().register(name(),
                                            new SessionFactoryHealthCheck(
                                                    environment.getHealthCheckExecutorService(),
                                                    primaryConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                                    this.sessionFactory = factory,
                                                    primaryConfig.getValidationQuery()));
    }

    private UnitOfWorkApplicationListener registerUnitOfWorkListenerIfAbsent(Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof UnitOfWorkApplicationListener) {
                return (UnitOfWorkApplicationListener) singleton;
            }
        }
        final UnitOfWorkApplicationListener listener = new UnitOfWorkApplicationListener();
        environment.jersey().register(listener);
        return listener;
    }

    public boolean isLazyLoadingEnabled() {
        return lazyLoadingEnabled;
    }

    public void setLazyLoadingEnabled(boolean lazyLoadingEnabled) {
        this.lazyLoadingEnabled = lazyLoadingEnabled;
    }

    public SessionFactory getSessionFactory() {
        return requireNonNull(sessionFactory);
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
