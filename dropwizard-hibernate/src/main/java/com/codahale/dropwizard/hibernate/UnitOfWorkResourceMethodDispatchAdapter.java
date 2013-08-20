package com.codahale.dropwizard.hibernate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import org.hibernate.SessionFactory;

import javax.ws.rs.ext.Provider;

@Provider
public class UnitOfWorkResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    public UnitOfWorkResourceMethodDispatchAdapter(SessionFactory sessionFactory) {
        final ImmutableMap.Builder<Optional<String>, SessionFactory> bldr = new ImmutableMap.Builder<>();
        bldr.put(Optional.<String> absent(), sessionFactory);
        sessionFactoryMap = bldr.build();
    }

    public UnitOfWorkResourceMethodDispatchAdapter(ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.sessionFactoryMap = sessionFactoryMap;
    }

    public ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }

    public SessionFactory getDefaultSessionFactory() {
        return sessionFactoryMap.get(Optional.absent());
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new UnitOfWorkResourceMethodDispatchProvider(provider, sessionFactoryMap);
    }
}
