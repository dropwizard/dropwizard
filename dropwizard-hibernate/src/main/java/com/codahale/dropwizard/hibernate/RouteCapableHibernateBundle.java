package com.codahale.dropwizard.hibernate;

import java.util.LinkedHashMap;
import java.util.Map;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.db.DataSourceFactory;
import com.codahale.dropwizard.db.DataSourceRoute;
import com.codahale.dropwizard.db.RouteCapableDatabaseConfiguration;
import com.codahale.dropwizard.setup.Environment;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.hibernate.SessionFactory;

public abstract class RouteCapableHibernateBundle<T extends Configuration> extends HibernateBundle<T> implements
        RouteCapableDatabaseConfiguration<T> {
    private ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    public ImmutableMap<Optional<String>, SessionFactory> getSessionFactories() {
        return sessionFactoryMap;
    }

    public RouteCapableHibernateBundle(Class<?> entity, Class<?>... entities) {
        super(entity, entities);
    }

    public RouteCapableHibernateBundle(ImmutableList<Class<?>> entities, SessionFactoryFactory sessionFactoryFactory) {
        super(entities, sessionFactoryFactory);
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final Map<Optional<String>, SessionFactory> sessionFactories = new LinkedHashMap<>();
        for (DataSourceRoute route : getDataSourceRoutes(configuration)) {
            final String routeKey = route.getRouteName();
            final DataSourceFactory dbConfig = route.getDatabase();

            final SessionFactory sessionFactory = sessionFactoryFactory.build(this, environment, dbConfig, entities,
                    routeKey);
            environment.healthChecks().register(routeKey,
                    new SessionFactoryHealthCheck(sessionFactory, dbConfig.getValidationQuery()));

            // the primary url will be the default route when no route key is provided
            if (sessionFactories.isEmpty()) {
                sessionFactories.put(Optional.<String> absent(), sessionFactory);
            }
            sessionFactories.put(Optional.of(routeKey), sessionFactory);
        }

        this.sessionFactoryMap = ImmutableMap.copyOf(sessionFactories);
        environment.jersey().register(new UnitOfWorkResourceMethodDispatchAdapter(this.sessionFactoryMap));
    }

    @Override
    public final DataSourceFactory getDataSourceFactory(T configuration) {
        return null; // no-op
    }
}
