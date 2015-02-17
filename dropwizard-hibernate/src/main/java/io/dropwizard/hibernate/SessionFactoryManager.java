package io.dropwizard.hibernate;

import com.google.common.annotations.VisibleForTesting;
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

    @VisibleForTesting
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
