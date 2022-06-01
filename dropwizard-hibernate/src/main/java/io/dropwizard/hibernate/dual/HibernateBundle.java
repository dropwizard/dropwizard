package io.dropwizard.hibernate.dual;

import static java.util.Objects.requireNonNull;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.hibernate.SessionFactoryFactory;
import io.dropwizard.hibernate.SessionFactoryHealthCheck;
import io.dropwizard.util.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.hibernate.SessionFactory;

public abstract class HibernateBundle<T> extends io.dropwizard.hibernate.HibernateBundle<T> {
    public static final String PRIMARY = ":hibernate-primary";
    public static final String READER = ":hibernate-reader";

    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        super(entity, entities);
    }

    protected HibernateBundle(List<Class<?>> entities, SessionFactoryFactory sessionFactoryFactory) {
        super(entities, sessionFactoryFactory);
    }

    public abstract PooledDataSourceFactory getReadSourceFactory(T configuration);

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        final String name = name();
        final String primaryName = name + PRIMARY;
        final String readerName = name + READER;
        final PooledDataSourceFactory primaryConfig = getDataSourceFactory(configuration);
        final SessionFactory primary =
                requireNonNull(sessionFactoryFactory.build(this, environment, primaryConfig, entities, primaryName));
        final PooledDataSourceFactory readerConfig = getReadSourceFactory(configuration);
        final SessionFactory reader =
                requireNonNull(sessionFactoryFactory.build(this, environment, readerConfig, entities, readerName));

        final DualSessionFactory factory = new DualSessionFactory(primary, reader);
        registerUnitOfWorkListenerIfAbsent(environment).registerSessionFactory(name, factory);

        final ExecutorService exec = environment.getHealthCheckExecutorService();
        environment
                .healthChecks()
                .register(
                        primaryName,
                        new SessionFactoryHealthCheck(
                                exec,
                                primaryConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                primary,
                                primaryConfig.getValidationQuery()));
        environment
                .healthChecks()
                .register(
                        readerName,
                        new SessionFactoryHealthCheck(
                                exec,
                                readerConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                reader,
                                readerConfig.getValidationQuery()));

        this.sessionFactory = factory;
    }
}
