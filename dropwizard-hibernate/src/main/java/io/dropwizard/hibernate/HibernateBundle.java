package io.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module.Feature;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class HibernateBundle<T extends Configuration> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    public static final String DEFAULT_NAME = "hibernate";

    @Nullable
    private ClusteredSessionFactory clusteredSessionFactory;
    private boolean lazyLoadingEnabled = true;

    private final List<Class<?>> entities;
    private final ClusteredSessionFactoryFactory clusteredSessionFactoryFactory;

    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        final List<Class<?>> entityClasses = new ArrayList<>();
        entityClasses.add(entity);
        entityClasses.addAll(Arrays.asList(entities));

        this.entities = Collections.unmodifiableList(entityClasses);
        this.clusteredSessionFactoryFactory = new ClusteredSessionFactoryFactory();
    }

    protected HibernateBundle(List<Class<?>> entities,
                              ClusteredSessionFactoryFactory clusteredSessionFactoryFactory) {
        this.entities = entities;
        this.clusteredSessionFactoryFactory = clusteredSessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(createHibernate5Module());
    }

    /**
     * Override to configure the {@link Hibernate5Module}.
     */
    protected Hibernate5Module createHibernate5Module() {
        Hibernate5Module module = new Hibernate5Module();
        if (lazyLoadingEnabled) {
            module.enable(Feature.FORCE_LAZY_LOADING);
        }
        return module;
    }

    /**
     * Override to configure the name of the bundle
     * (It's used for the bundle health check and database pool metrics)
     */
    protected String name() {
        return DEFAULT_NAME;
    }

    @Override
    public final void run(T configuration, Environment environment) {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.clusteredSessionFactory = requireNonNull(clusteredSessionFactoryFactory.build(this, environment, dbConfig,
            entities, name()));
        registerUnitOfWorkListenerIfAbsent(environment).registerSessionFactory(name(), clusteredSessionFactory);
        environment.healthChecks().register(name(),
                                            new SessionFactoryHealthCheck(
                                                    environment.getHealthCheckExecutorService(),
                                                    dbConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                                    clusteredSessionFactory.getSessionFactory(),
                                                    dbConfig.getValidationQuery()));
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

    public ClusteredSessionFactory getSessionFactory() {
        return requireNonNull(clusteredSessionFactory);
    }

    protected void configure(org.hibernate.cfg.Configuration configuration) {
    }
}
