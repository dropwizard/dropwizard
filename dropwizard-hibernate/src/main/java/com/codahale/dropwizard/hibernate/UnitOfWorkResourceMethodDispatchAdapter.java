package com.codahale.dropwizard.hibernate;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import org.hibernate.SessionFactory;

import javax.ws.rs.ext.Provider;

@Provider
public class UnitOfWorkResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final SessionFactory sessionFactory;

    public UnitOfWorkResourceMethodDispatchAdapter(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new UnitOfWorkResourceMethodDispatchProvider(provider, sessionFactory);
    }
}
