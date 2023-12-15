package io.dropwizard.hibernate;

import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.db.PooledDataSourceFactory;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.BootstrapServiceRegistry;
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder;
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
import java.util.stream.Collectors;

public class SessionFactoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryFactory.class);
    private static final String DEFAULT_NAME = "hibernate";

    public SessionFactory build(HibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                List<Class<?>> entities) {
        return build(bundle, environment, dbConfig, entities, DEFAULT_NAME);
    }

    public SessionFactory build(HibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                List<Class<?>> entities,
                                String name) {
        final ManagedDataSource dataSource = dbConfig.build(environment.metrics(), name);
        return build(bundle, environment, dbConfig, dataSource, entities);
    }

    public SessionFactory build(HibernateBundle<?> bundle,
                                Environment environment,
                                PooledDataSourceFactory dbConfig,
                                ManagedDataSource dataSource,
                                List<Class<?>> entities) {
        final ConnectionProvider provider = buildConnectionProvider(dataSource,
            dbConfig.getProperties());
        final SessionFactory factory = buildSessionFactory(bundle,
            dbConfig,
            provider,
            dbConfig.getProperties(),
            entities);
        final SessionFactoryManager managedFactory = new SessionFactoryManager(factory, dataSource);
        environment.lifecycle().manage(managedFactory);
        return factory;
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource,
                                                       Map<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        Map<String, Object> newProperties = properties.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        connectionProvider.configure(newProperties);
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(HibernateBundle<?> bundle,
                                               PooledDataSourceFactory dbConfig,
                                               ConnectionProvider connectionProvider,
                                               Map<String, String> properties,
                                               List<Class<?>> entities) {

        final BootstrapServiceRegistry bootstrapServiceRegistry =
            configureBootstrapServiceRegistryBuilder(new BootstrapServiceRegistryBuilder()).build();

        final Configuration configuration = new Configuration(bootstrapServiceRegistry);
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, Boolean.toString(dbConfig.isAutoCommentsEnabled()));
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");

        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        addAnnotatedClasses(configuration, entities);
        bundle.configure(configuration);

        final ServiceRegistry registry = new StandardServiceRegistryBuilder(bootstrapServiceRegistry)
            .addService(ConnectionProvider.class, connectionProvider)
            .applySettings(configuration.getProperties())
            .build();

        configure(configuration, registry);

        return configuration.buildSessionFactory(registry);
    }

    protected void configure(Configuration configuration, ServiceRegistry registry) {
        // Default implementation is a no-op
    }

    protected BootstrapServiceRegistryBuilder configureBootstrapServiceRegistryBuilder(BootstrapServiceRegistryBuilder builder) {
        return builder;
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
