package io.dropwizard.hibernate;

import io.dropwizard.hibernate.dual.DualSessionFactory;
import org.jspecify.annotations.Nullable;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * An aspect providing operations around a method with the {@link UnitOfWork} annotation.
 * It opens a Hibernate session and optionally a transaction.
 * <p>It should be created for every invocation of the method.</p>
 * <p>Usage :</p>
 * <pre>
 * {@code
 *   UnitOfWorkProxyFactory unitOfWorkProxyFactory = ...
 *   UnitOfWork unitOfWork = ...         // get annotation from method.
 *
 *   UnitOfWorkAspect aspect = unitOfWorkProxyFactory.newAspect();
 *   try {
 *     aspect.beforeStart(unitOfWork);
 *     ...                               // perform business logic.
 *     aspect.afterEnd();
 *   } catch (Exception e) {
 *     aspect.onError();
 *     throw e;
 *   } finally {
 *     aspect.onFinish();
 *   }
 * }
 * </pre>
 */
public class UnitOfWorkAspect {

    private final Map<String, SessionFactory> sessionFactories;

    public UnitOfWorkAspect(Map<String, SessionFactory> sessionFactories) {
        this.sessionFactories = sessionFactories;
    }

    // Context variables
    @Nullable
    private UnitOfWork unitOfWork;

    @Nullable
    private Session session;

    @Nullable
    private SessionFactory sessionFactory;

    // was the session created by this aspect?
    private boolean sessionCreated;
    // do we manage the transaction or did we join an existing one?
    private boolean transactionStarted;

    public void beforeStart(@Nullable UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            return;
        }
        this.unitOfWork = unitOfWork;

        sessionFactory = sessionFactories.get(unitOfWork.value());
        if (sessionFactory == null) {
            // If the user didn't specify the name of a session factory,
            // and we have only one registered, we can assume that it's the right one.
            if (unitOfWork.value().equals(HibernateBundle.DEFAULT_NAME) && sessionFactories.size() == 1) {
                sessionFactory = sessionFactories.values().iterator().next();
            } else {
                throw new IllegalArgumentException("Unregistered Hibernate bundle: '" + unitOfWork.value() + "'");
            }
        }

        if (sessionFactory instanceof DualSessionFactory dualSessionFactory) {
            dualSessionFactory.prepare(unitOfWork.readOnly());
        }

        Session existingSession = null;
        if(ManagedSessionContext.hasBind(sessionFactory)) {
            existingSession = sessionFactory.getCurrentSession();
        }

        if(existingSession != null) {
            sessionCreated = false;
            session = existingSession;
            validateSession();
        } else {
            sessionCreated = true;
            session = sessionFactory.openSession();
            try {
                configureSession();
                ManagedSessionContext.bind(session);
            } catch (Throwable th) {
                session.close();
                session = null;
                ManagedSessionContext.unbind(sessionFactory);
                throw th;
            }
        }
        beginTransaction(unitOfWork, session);
    }

    public void afterEnd() {
        if (unitOfWork == null || session == null) {
            return;
        }

        try {
            commitTransaction(unitOfWork, session);
        } catch (Exception e) {
            rollbackTransaction(unitOfWork, session);
            throw e;
        }
        // We should not close the session to let the lazy loading work during serializing a response to the client.
        // If the response successfully serialized, then the session will be closed by the `onFinish` method
    }

    public void onError() {
        if (unitOfWork == null || session == null) {
            return;
        }

        try {
            rollbackTransaction(unitOfWork, session);
        } finally {
            onFinish();
        }
    }

    public void onFinish() {
        try {
            if (sessionCreated && session != null) {
                session.close();
            }
        } finally {
            session = null;
            if(sessionCreated) {
                ManagedSessionContext.unbind(sessionFactory);
            }
        }
    }

    protected void configureSession() {
        if (unitOfWork == null || session == null) {
            throw new NullPointerException("unitOfWork or session is null. This is a bug!");
        }
        session.setDefaultReadOnly(unitOfWork.readOnly());
        session.setCacheMode(unitOfWork.cacheMode());
        session.setHibernateFlushMode(unitOfWork.flushMode());
    }

    protected void validateSession() {
        if (unitOfWork == null || session == null) {
            throw new NullPointerException("unitOfWork or session is null. This is a bug!");
        }
        if(unitOfWork.readOnly() != session.isDefaultReadOnly()) {
            throw new IllegalStateException(String.format(
                "Existing session readOnly state (%b) does not match requested state (%b)",
                session.isDefaultReadOnly(),
                unitOfWork.readOnly()
            ));
        }
        if(unitOfWork.cacheMode() != session.getCacheMode()) {
            throw new IllegalStateException(String.format(
                "Existing session cache mode (%s) does not match requested mode (%s)",
                session.getCacheMode(),
                unitOfWork.cacheMode()
            ));
        }
        if(unitOfWork.flushMode() != session.getHibernateFlushMode()) {
            throw new IllegalStateException(String.format(
                "Existing session flush mode (%s) does not match requested mode (%s)",
                session.getHibernateFlushMode(),
                unitOfWork.flushMode()
            ));
        }
    }

    private void beginTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if(txn != null && txn.isActive()) {
            transactionStarted = false;
        } else {
            session.beginTransaction();
            transactionStarted = true;
        }
    }

    private void rollbackTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (transactionStarted && txn != null && txn.getStatus().canRollback()) {
            txn.rollback();
        }
    }

    private void commitTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (transactionStarted && txn != null && txn.getStatus().canRollback()) {
            txn.commit();
        }
    }

    protected Session getSession() {
        return requireNonNull(session);
    }

    protected SessionFactory getSessionFactory() {
        return requireNonNull(sessionFactory);
    }

}
