package io.dropwizard.hibernate;

import io.dropwizard.db.ManagedDataSources;
import io.dropwizard.db.PooledDataSourceFactory;
import io.dropwizard.setup.Environment;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class ClusteredSessionFactoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusteredSessionFactoryFactory.class);
    private static final String DEFAULT_NAME = "hibernate";

    public ClusteredSessionFactory build(HibernateBundle<?> bundle,
                                         Environment environment,
                                         PooledDataSourceFactory dbConfig,
                                         List<Class<?>> entities) {
        return build(bundle, environment, dbConfig, entities, DEFAULT_NAME);
    }

    public ClusteredSessionFactory build(HibernateBundle<?> bundle,
                                         Environment environment,
                                         PooledDataSourceFactory dbConfig,
                                         List<Class<?>> entities,
                                         String name) {
        final ManagedDataSources dataSources = dbConfig.build(environment.metrics(), name);
        return build(bundle, environment, dbConfig, dataSources, entities);
    }

    public ClusteredSessionFactory build(HibernateBundle<?> bundle,
                                         Environment environment,
                                         PooledDataSourceFactory dbConfig,
                                         ManagedDataSources dataSources,
                                         List<Class<?>> entities) {
        final ConnectionProvider writeProvider = buildConnectionProvider(dataSources.getWriteDataSource(),
                                                                         dbConfig.getProperties());
        final SessionFactory writeFactory = buildSessionFactory(bundle,
                                                                dbConfig,
                                                                writeProvider,
                                                                dbConfig.getProperties(),
                                                                entities);

        final SessionFactoryManager managedFactory = new SessionFactoryManager(writeFactory, dataSources.getWriteDataSource());
        environment.lifecycle().manage(managedFactory);
        SessionFactory readFactory = writeFactory;

        if (dataSources.hasSeparateReader()) {
            final ConnectionProvider readProvider = buildConnectionProvider(dataSources.getReadDataSource(),
                dbConfig.getProperties());
            readFactory = buildSessionFactory(bundle,
                dbConfig,
                readProvider,
                dbConfig.getProperties(),
                entities);

            final SessionFactoryManager managedReadFactory = new SessionFactoryManager(writeFactory, dataSources.getWriteDataSource());
            environment.lifecycle().manage(managedReadFactory);
        }

        return new ClusteredSessionFactory(writeFactory, readFactory);
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource,
                                                       Map<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(HibernateBundle<?> bundle,
                                               PooledDataSourceFactory dbConfig,
                                               ConnectionProvider connectionProvider,
                                               Map<String, String> properties,
                                               List<Class<?>> entities) {
        final Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.toString(dbConfig.isAutoCommentsEnabled()));
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
        configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        configuration.setProperty("jadira.usertype.autoRegisterUserTypes", "true");
        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        addAnnotatedClasses(configuration, entities);
        bundle.configure(configuration);

        final ServiceRegistry registry = new StandardServiceRegistryBuilder()
                .addService(ConnectionProvider.class, connectionProvider)
                .applySettings(configuration.getProperties())
                .build();

        configure(configuration, registry);

        return configuration.buildSessionFactory(registry);
    }

    protected void configure(Configuration configuration, ServiceRegistry registry) {
    }

    private void addAnnotatedClasses(Configuration configuration,
                                     Iterable<Class<?>> entities) {
        final SortedSet<String> entityClasses = new TreeSet<>();
        for (Class<?> klass : entities) {
            configuration.addAnnotatedClass(klass);
            entityClasses.add(klass.getCanonicalName());
        }
        LOGGER.info("Entity classes: {}", entityClasses);
    }
}
