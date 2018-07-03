package io.dropwizard.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

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

    private final Map<String, ClusteredSessionFactory> clusteredSessionFactories;

    public UnitOfWorkAspect(Map<String, ClusteredSessionFactory> clusteredSessionFactories) {
        this.clusteredSessionFactories = clusteredSessionFactories;
    }

    // Context variables
    @Nullable
    private UnitOfWork unitOfWork;

    @Nullable
    private Session session;

    @Nullable
    private ClusteredSessionFactory clusteredSessionFactory;

    public void beforeStart(@Nullable UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            return;
        }
        this.unitOfWork = unitOfWork;

        clusteredSessionFactory = clusteredSessionFactories.get(unitOfWork.value());
        if (clusteredSessionFactory == null) {
            // If the user didn't specify the name of a session factory,
            // and we have only one registered, we can assume that it's the right one.
            if (unitOfWork.value().equals(HibernateBundle.DEFAULT_NAME) && clusteredSessionFactories.size() == 1) {
                clusteredSessionFactory = clusteredSessionFactories.values().iterator().next();
            } else {
                throw new IllegalArgumentException("Unregistered Hibernate bundle: '" + unitOfWork.value() + "'");
            }
        }
        clusteredSessionFactory.setReadOnly(unitOfWork.readOnly());
        session = clusteredSessionFactory.getSessionFactory().openSession();

        try {
            configureSession();
            ManagedSessionContext.bind(session);
            beginTransaction(unitOfWork, session);
        } catch (Throwable th) {
            session.close();
            session = null;
            ManagedSessionContext.unbind(clusteredSessionFactory.getSessionFactory());
            throw th;
        }
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
            if (session != null) {
                session.close();
            }
        } finally {
            session = null;
            if (clusteredSessionFactory != null) {
                ManagedSessionContext.unbind(clusteredSessionFactory.getSessionFactory());
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

    private void beginTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        session.beginTransaction();
    }

    private void rollbackTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.getStatus().canRollback()) {
            txn.rollback();
        }
    }

    private void commitTransaction(UnitOfWork unitOfWork, Session session) {
        if (!unitOfWork.transactional()) {
            return;
        }
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.getStatus().canRollback()) {
            txn.commit();
        }
    }

    protected Session getSession() {
        return requireNonNull(session);
    }

    protected SessionFactory getSessionFactory() {
        requireNonNull(this.clusteredSessionFactory);
        return requireNonNull(clusteredSessionFactory.getSessionFactory());
    }

}
