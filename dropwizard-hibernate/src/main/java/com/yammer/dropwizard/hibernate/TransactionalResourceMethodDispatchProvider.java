package com.yammer.dropwizard.hibernate;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.SessionFactory;

public class TransactionalResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
    private final ResourceMethodDispatchProvider provider;
    private final SessionFactory sessionFactory;

    public TransactionalResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider,
                                                       SessionFactory sessionFactory) {
        this.provider = provider;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod) {
        final RequestDispatcher dispatcher = provider.create(abstractResourceMethod);
        final Transactional transactional = abstractResourceMethod.getMethod()
                                                                  .getAnnotation(Transactional.class);
        if (transactional != null) {
            return new TransactionalRequestDispatcher(transactional, dispatcher, sessionFactory);
        }
        return dispatcher;
    }
}
