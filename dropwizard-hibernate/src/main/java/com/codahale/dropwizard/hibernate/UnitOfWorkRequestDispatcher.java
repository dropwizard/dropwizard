package com.codahale.dropwizard.hibernate;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.NotFoundException;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

public class UnitOfWorkRequestDispatcher implements RequestDispatcher {
    public static final String ROUTE_KEY_HEADER_NAME = "RouteKey";

    private final UnitOfWork unitOfWork;
    private final RequestDispatcher dispatcher;
    private final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap;

    public UnitOfWorkRequestDispatcher(UnitOfWork unitOfWork, RequestDispatcher dispatcher,
            SessionFactory sessionFactory) {
        this.unitOfWork = unitOfWork;
        this.dispatcher = dispatcher;

        final ImmutableMap.Builder<Optional<String>, SessionFactory> bldr = new ImmutableMap.Builder<>();
        bldr.put(Optional.<String> absent(), sessionFactory);
        sessionFactoryMap = bldr.build();
    }

    public UnitOfWorkRequestDispatcher(final UnitOfWork unitOfWork, final RequestDispatcher dispatcher,
            final ImmutableMap<Optional<String>, SessionFactory> sessionFactoryMap) {
        this.unitOfWork = unitOfWork;
        this.dispatcher = dispatcher;
        this.sessionFactoryMap = checkNotNull(sessionFactoryMap);
    }

    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    public RequestDispatcher getDispatcher() {
        return dispatcher;
    }

    public ImmutableMap<Optional<String>, SessionFactory> getSessionFactoryMap() {
        return sessionFactoryMap;
    }

    public SessionFactory getDefaultSessionFactory() {
        return sessionFactoryMap.get(Optional.absent());
    }

    @Override
    public void dispatch(Object resource, HttpContext context) {
        final Session session = route(context).openSession();
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
            ManagedSessionContext.unbind(route(context));
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

    /**
     * Retrieves the route key header ('RouteKey') and returns the corresponding
     * {@link SessionFactory}.
     * 
     * @param context
     *            the {@link HttpContext}
     * @return the corresponding {@link SessionFactory}
     * @throws NotFoundException
     *             if a {@link SessionFactory} can not be found for the given
     *             route key
     */
    private SessionFactory route(final HttpContext context) {
        final String routeKey = context.getRequest().getHeaderValue(
                ROUTE_KEY_HEADER_NAME);
        final SessionFactory factory = sessionFactoryMap.get(Optional
                .fromNullable(routeKey));
        if (null == factory) {
            throw new NotFoundException("No SessionFactory found for RouteKey["
                    + routeKey + "]");
        }

        return factory;
    }
}
