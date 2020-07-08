package io.dropwizard.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
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
    private static final Logger logger = LoggerFactory.getLogger(UnitOfWorkAspect.class);
    private final Map<String, SessionFactory> sessionFactories;

    public UnitOfWorkAspect(Map<String, SessionFactory> sessionFactories) {
        this.sessionFactories = sessionFactories;
    }

    // was the session created by this aspect?
    private boolean sessionCreated;
    // do we manage the transaction or did we join an existing one?
    private boolean transactionStarted;

    public void beforeStart(@Nullable UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            return;
        }

        SessionFactory sessionFactory = sessionFactories.get(unitOfWork.value());
        if (sessionFactory == null) {
            // If the user didn't specify the name of a session factory,
            // and we have only one registered, we can assume that it's the right one.
            if (unitOfWork.value().equals(HibernateBundle.DEFAULT_NAME) && sessionFactories.size() == 1) {
                sessionFactory = sessionFactories.values().iterator().next();
            } else {
                throw new IllegalArgumentException("Unregistered Hibernate bundle: '" + unitOfWork.value() + "'");
            }
        }

        Session existingSession = null;

        if (ManagedSessionContext.hasBind(sessionFactory)) {
            existingSession = sessionFactory.getCurrentSession();
        }

        if (existingSession != null && existingSession.isOpen() && existingSession.getSessionFactory() != null && UnitOfWorkContext.getSessionFactory() != null) {
            logger.info("Reusing existing session");
            sessionCreated = false;
        } else {
            Session session = null;
            try {
                session = sessionFactory.openSession();
                sessionCreated = true;
                setContext(unitOfWork, session);
                configureSession();
                beginTransaction(unitOfWork, session)   ;
            } catch (Throwable th) {
                if (session != null) {
                    session.close();
                }
                clearContext();
                throw th;
            }
        }
    }

    public void afterEnd() {
        UnitOfWork unitOfWork = UnitOfWorkContext.getUnitOfWork();
        SessionFactory sessionFactory = UnitOfWorkContext.getSessionFactory();
        if (unitOfWork == null || sessionFactory == null) {
            return;
        }

        try {
            commitTransaction(unitOfWork, sessionFactory.getCurrentSession());
        } catch (Exception e) {
            rollbackTransaction(unitOfWork, sessionFactory.getCurrentSession());
            throw e;
        }
        // We should not close the session to let the lazy loading work during serializing a response to the client.
        // If the response successfully serialized, then the session will be closed by the `onFinish` method
    }

    public void onError() {
        UnitOfWork unitOfWork = UnitOfWorkContext.getUnitOfWork();
        SessionFactory sessionFactory = UnitOfWorkContext.getSessionFactory();
        if (unitOfWork == null || sessionFactory == null) {
            return;
        }

        try {
            rollbackTransaction(unitOfWork, sessionFactory.getCurrentSession());
        } finally {
            onFinish();
        }
    }

    public void onFinish() {
        try {
            SessionFactory sessionFactory = UnitOfWorkContext.getSessionFactory();
            if (sessionCreated && sessionFactory != null) {
                sessionFactory.getCurrentSession().close();
            }
        } finally {
            if (sessionCreated) {
                clearContext();
            }
        }
    }

    protected void configureSession() {
        Session session = UnitOfWorkContext.getCurrentSession();
        UnitOfWork unitOfWork = requireNonNull(UnitOfWorkContext.getUnitOfWork());
        session.setDefaultReadOnly(unitOfWork.readOnly());
        session.setCacheMode(unitOfWork.cacheMode());
        session.setHibernateFlushMode(unitOfWork.flushMode());
    }

    private void beginTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.isActive()) {
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

    private static void setContext(UnitOfWork unitOfWork, Session session) {
        clearContext();
        ManagedSessionContext.bind(session);
        UnitOfWorkContext.setUnitOfWork(unitOfWork);
        SessionFactory sessionFactory = session.getSessionFactory();
        if (sessionFactory == null) {
            clearContext();
            throw new IllegalStateException("No session factory on session");
        }
        UnitOfWorkContext.setSessionFactory(sessionFactory);
    }

    private static void clearContext() {
        ManagedSessionContext.unbind(UnitOfWorkContext.getSessionFactory());
        UnitOfWorkContext.clear();
    }
}
