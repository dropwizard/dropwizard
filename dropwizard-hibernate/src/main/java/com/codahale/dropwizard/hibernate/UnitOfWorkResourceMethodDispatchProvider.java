package com.codahale.dropwizard.hibernate;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.SessionFactory;

public class UnitOfWorkResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    public UnitOfWorkResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider,
            ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.provider = provider;
        this.sessionFactoryMap = sessionFactoryMap;
    }

    public ResourceMethodDispatchProvider getProvider() {
        return provider;
    }

    public ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }

    public SessionFactory getDefaultSessionFactory() {
        return sessionFactoryMap.get(Optional.absent());
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final UnitOfWork unitOfWork = abstractResourceMethod.getMethod().getAnnotation(UnitOfWork.class);
        if (unitOfWork != null) {
            return new UnitOfWorkRequestDispatcher(unitOfWork, dispatcher, sessionFactoryMap);
        }
        return dispatcher;
    }
}
