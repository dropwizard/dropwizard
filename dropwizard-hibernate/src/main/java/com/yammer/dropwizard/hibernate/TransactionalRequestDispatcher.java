package com.yammer.dropwizard.hibernate;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class TransactionalRequestDispatcher implements RequestDispatcher {
    private final RequestDispatcher dispatcher;
    private final SessionFactory sessionFactory;

    public TransactionalRequestDispatcher(RequestDispatcher dispatcher,
                                          SessionFactory sessionFactory) {
        this.dispatcher = dispatcher;
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void dispatch(Object resource, HttpContext context) {
        final Session session = sessionFactory.openSession();
        try {
            ManagedSessionContext.bind(session);
            final Transaction txn = session.beginTransaction();
            try {
                dispatcher.dispatch(resource, context);
                if (txn.isActive()) {
                    txn.commit();
                }
            } catch (Exception e) {
                if (txn.isActive()) {
                    txn.rollback();
                }
                this.<RuntimeException>rethrow(e);
            }
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
    }
}
