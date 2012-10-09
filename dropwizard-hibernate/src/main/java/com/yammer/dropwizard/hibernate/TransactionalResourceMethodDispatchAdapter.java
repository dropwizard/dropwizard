package com.yammer.dropwizard.hibernate;

import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import org.hibernate.SessionFactory;

import javax.ws.rs.ext.Provider;

@Provider
public class TransactionalResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final SessionFactory sessionFactory;

    public TransactionalResourceMethodDispatchAdapter(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new TransactionalResourceMethodDispatchProvider(provider, sessionFactory);
    }
}
