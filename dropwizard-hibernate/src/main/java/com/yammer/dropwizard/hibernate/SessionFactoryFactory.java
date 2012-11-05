package com.yammer.dropwizard.hibernate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

public class SessionFactoryFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionFactoryFactory.class);

    private final ManagedDataSourceFactory dataSourceFactory = new ManagedDataSourceFactory();

    public SessionFactory build(Environment environment,
                                DatabaseConfiguration dbConfig,
                                List<String> packages) throws ClassNotFoundException {
        final ManagedDataSource dataSource = dataSourceFactory.build(dbConfig);
        environment.manage(dataSource);
        return buildSessionFactory(buildConnectionProvider(dataSource, dbConfig.getProperties()),
                                   dbConfig.getProperties(),
                                   packages);
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource,
                                                       ImmutableMap<String, String> properties) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(properties);
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(ConnectionProvider connectionProvider,
                                               ImmutableMap<String, String> properties,
                                               List<String> packages) {
        final Configuration configuration = new Configuration();
        configuration.setProperty(AvailableSettings.CURRENT_SESSION_CONTEXT_CLASS, "managed");
        configuration.setProperty(AvailableSettings.USE_SQL_COMMENTS, "true");
        configuration.setProperty(AvailableSettings.USE_GET_GENERATED_KEYS, "true");
        configuration.setProperty(AvailableSettings.GENERATE_STATISTICS, "true");
        configuration.setProperty(AvailableSettings.USE_REFLECTION_OPTIMIZER, "true");
        configuration.setProperty(AvailableSettings.ORDER_UPDATES, "true");
        configuration.setProperty(AvailableSettings.ORDER_INSERTS, "true");
        configuration.setProperty(AvailableSettings.USE_NEW_ID_GENERATOR_MAPPINGS, "true");
        for (Map.Entry<String, String> property : properties.entrySet()) {
            configuration.setProperty(property.getKey(), property.getValue());
        }

        addAnnotatedClasses(configuration, packages.toArray(new String[packages.size()]));

        final ServiceRegistry registry = new ServiceRegistryBuilder()
                .addService(ConnectionProvider.class, connectionProvider)
                .applySettings(properties)
                .buildServiceRegistry();

        return configuration.buildSessionFactory(registry);
    }

    private void addAnnotatedClasses(Configuration configuration, String[] packages) {
        @SuppressWarnings("unchecked")
        final AnnotationScannerListener scannerListener = new AnnotationScannerListener(Entity.class);
        final PackageNamesScanner scanner = new PackageNamesScanner(packages);
        scanner.scan(scannerListener);
        final SortedSet<String> entityClasses = Sets.newTreeSet();
        for (Class<?> klass : scannerListener.getAnnotatedClasses()) {
            configuration.addAnnotatedClass(klass);
            entityClasses.add(klass.getCanonicalName());
        }
        LOGGER.info("Entity classes: {}", entityClasses);
    }
}
