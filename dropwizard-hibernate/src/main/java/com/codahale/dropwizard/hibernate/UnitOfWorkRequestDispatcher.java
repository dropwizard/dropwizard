package com.codahale.dropwizard.hibernate;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class UnitOfWorkRequestDispatcher implements RequestDispatcher {
    private final UnitOfWork unitOfWork;
    private final RequestDispatcher dispatcher;
    private final SessionFactory sessionFactory;

    public UnitOfWorkRequestDispatcher(UnitOfWork unitOfWork,
                                       RequestDispatcher dispatcher,
                                       SessionFactory sessionFactory) {
        this.unitOfWork = unitOfWork;
        this.dispatcher = dispatcher;
        this.sessionFactory = sessionFactory;
    }

    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void dispatch(Object resource, HttpContext context) {
        final Session session = sessionFactory.openSession();
        try {
            configureSession(session);
            ManagedSessionContext.bind(session);
            beginTransaction(session);
            try {
                dispatcher.dispatch(resource, context);
                commitTransaction(session);
            } catch (Exception e) {
                rollbackTransaction(session);
                this.<RuntimeException>rethrow(e);
            }
        } finally {
            session.close();
            ManagedSessionContext.unbind(sessionFactory);
        }
    }

    private void beginTransaction(Session session) {
        if (unitOfWork.transactional()) {
            session.beginTransaction();
        }
    }

    private void configureSession(Session session) {
        session.setDefaultReadOnly(unitOfWork.readOnly());
        session.setCacheMode(unitOfWork.cacheMode());
        session.setFlushMode(unitOfWork.flushMode());
    }

    private void rollbackTransaction(Session session) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private void commitTransaction(Session session) {
        if (unitOfWork.transactional()) {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.isActive()) {
                txn.commit();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <E extends Exception> void rethrow(Exception e) throws E {
        throw (E) e;
    }
}
