package io.dropwizard.hibernate;

import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule.Feature;
import io.dropwizard.core.ConfiguredBundle;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DatabaseConfiguration;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.util.Duration;
import org.jspecify.annotations.Nullable;
import org.hibernate.SessionFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public abstract class HibernateBundle<T> implements ConfiguredBundle<T>, DatabaseConfiguration<T> {
    public static final String DEFAULT_NAME = "hibernate";

    @Nullable
    protected SessionFactory sessionFactory;
    protected boolean lazyLoadingEnabled = true;

    protected final List<Class<?>> entities;
    protected final SessionFactoryFactory sessionFactoryFactory;

    protected HibernateBundle(Class<?> entity, Class<?>... entities) {
        final List<Class<?>> entityClasses = new ArrayList<>();
        entityClasses.add(entity);
        entityClasses.addAll(Arrays.asList(entities));

        this.entities = Collections.unmodifiableList(entityClasses);
        this.sessionFactoryFactory = new SessionFactoryFactory();
    }

    protected HibernateBundle(List<Class<?>> entities,
                              SessionFactoryFactory sessionFactoryFactory) {
        this.entities = entities;
        this.sessionFactoryFactory = sessionFactoryFactory;
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        bootstrap.getObjectMapper().registerModule(createHibernate5Module());
    }

    /**
     * Override to configure the {@link Hibernate5JakartaModule}.
     */
    protected Hibernate5JakartaModule createHibernate5Module() {
        Hibernate5JakartaModule module = new Hibernate5JakartaModule();
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
    public void run(T configuration, Environment environment) throws Exception {
        final PooledDataSourceFactory dbConfig = getDataSourceFactory(configuration);
        this.sessionFactory = requireNonNull(sessionFactoryFactory.build(this, environment, dbConfig,
            entities, name()));
        registerUnitOfWorkListenerIfAbsent(environment).registerSessionFactory(name(), sessionFactory);
        environment.healthChecks().register(name(),
                                            new SessionFactoryHealthCheck(
                                                    environment.getHealthCheckExecutorService(),
                                                    dbConfig.getValidationQueryTimeout().orElse(Duration.seconds(5)),
                                                    sessionFactory,
                                                    dbConfig.getValidationQuery()));
    }

    protected UnitOfWorkApplicationListener registerUnitOfWorkListenerIfAbsent(Environment environment) {
        for (Object singleton : environment.jersey().getResourceConfig().getSingletons()) {
            if (singleton instanceof UnitOfWorkApplicationListener listener) {
                return listener;
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
