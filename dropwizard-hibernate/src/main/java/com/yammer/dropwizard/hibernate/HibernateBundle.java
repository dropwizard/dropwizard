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

public abstract class HibernateBundle<T extends Configuration> extends ConfiguredBundle<T> implements ConfigurationStrategy<T> {
    private SessionFactory sessionFactory;

    private final ImmutableList<String> packages;

    protected HibernateBundle(String... packages) {
        this.packages = ImmutableList.copyOf(packages);
    }

    @Override
    public final void initialize(Bootstrap<?> bootstrap) {
        final Hibernate4Module module = new Hibernate4Module();
        bootstrap.getObjectMapperFactory().registerModule(module);
    }

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        final DatabaseConfiguration dbConfig = getDatabaseConfiguration(configuration);
        this.sessionFactory = new SessionFactoryFactory(environment).build(dbConfig, packages);

        environment.addFilter(new SessionAutoCommitFilter(sessionFactory), "/*");
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
