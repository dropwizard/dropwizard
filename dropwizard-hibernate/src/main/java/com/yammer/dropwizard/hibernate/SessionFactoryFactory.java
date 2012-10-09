package com.yammer.dropwizard.hibernate;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.sun.jersey.core.spi.scanning.PackageNamesScanner;
import com.sun.jersey.spi.scanning.AnnotationScannerListener;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.db.ManagedDataSourceFactory;
import com.yammer.dropwizard.logging.Log;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;

import javax.persistence.Entity;
import javax.sql.DataSource;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

public class SessionFactoryFactory {
    private static final Log LOG = Log.forClass(SessionFactoryFactory.class);
    private final com.yammer.dropwizard.config.Environment environment;

    public SessionFactoryFactory(com.yammer.dropwizard.config.Environment environment) {
        this.environment = environment;
    }

    public SessionFactory build(DatabaseConfiguration dbConfig, List<String> packages) throws ClassNotFoundException {
        final ManagedDataSourceFactory dataSourceFactory = new ManagedDataSourceFactory(dbConfig);
        final ManagedDataSource dataSource = dataSourceFactory.build();
        environment.manage(dataSource);
        return buildSessionFactory(buildConnectionProvider(dataSource), packages);
    }

    public SessionFactory build(DataSource dataSource, List<String> packages) throws ClassNotFoundException {
        final ConnectionProvider connectionProvider = buildConnectionProvider(dataSource);
        return buildSessionFactory(connectionProvider, packages);
    }

    private ConnectionProvider buildConnectionProvider(DataSource dataSource) {
        final DatasourceConnectionProviderImpl connectionProvider = new DatasourceConnectionProviderImpl();
        connectionProvider.setDataSource(dataSource);
        connectionProvider.configure(Maps.newHashMap());
        return connectionProvider;
    }

    private SessionFactory buildSessionFactory(ConnectionProvider connectionProvider, List<String> packages) {
        final Configuration configuration = new Configuration();
        configuration.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS,
                                  LazilyManagedSessionContext.class.getCanonicalName());
        addAnnotatedClasses(configuration, packages.toArray(new String[packages.size()]));

        final Properties properties = new Properties();
        final ServiceRegistry registry = new ServiceRegistryBuilder()
                .applySettings(properties)
                .addService(ConnectionProvider.class, connectionProvider)
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
        LOG.info("Entity classes: {}", entityClasses);
    }
}
