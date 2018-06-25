package io.dropwizard.hibernate;

import io.dropwizard.db.ManagedDataSource;
import io.dropwizard.lifecycle.Managed;
import org.hibernate.SessionFactory;

public class SessionFactoryManager implements Managed {
    private final SessionFactory factory;
    private final ManagedDataSource dataSource;

    public SessionFactoryManager(SessionFactory factory, ManagedDataSource dataSource) {
        this.factory = factory;
        this.dataSource = dataSource;
    }

    ManagedDataSource getDataSource() {
        return dataSource;
    }

    @Override
    public void start() throws Exception {
        dataSource.start();
    }

    @Override
    public void stop() throws Exception {
        factory.close();
        dataSource.stop();
    }
}
