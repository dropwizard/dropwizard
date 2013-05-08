package com.codahale.dropwizard.hibernate;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.SessionFactory;

public class UnitOfWorkResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final SessionFactory sessionFactory;

    public UnitOfWorkResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider,
                                                    SessionFactory sessionFactory) {
        this.provider = provider;
        this.sessionFactory = sessionFactory;
    }

    public ResourceMethodDispatchProvider getProvider() {
        return provider;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final UnitOfWork unitOfWork = abstractResourceMethod.getMethod().getAnnotation(UnitOfWork.class);
        if (unitOfWork != null) {
            return new UnitOfWorkRequestDispatcher(unitOfWork, dispatcher, sessionFactory);
        }
        return dispatcher;
    }
}
