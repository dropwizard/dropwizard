package com.codahale.dropwizard.hibernate;

import com.codahale.dropwizard.db.ManagedDataSource;
import com.codahale.dropwizard.lifecycle.Managed;
import org.hibernate.SessionFactory;

public class SessionFactoryManager implements Managed {
    private final SessionFactory factory;
    private final ManagedDataSource dataSource;

    public SessionFactoryManager(SessionFactory factory, ManagedDataSource dataSource) {
        this.factory = factory;
        this.dataSource = dataSource;
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        factory.close();
        dataSource.stop();
    }
}
