package io.dropwizard.hibernate;

import org.apache.commons.lang3.tuple.Pair;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
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

    private Map<String, Session> sessions = new HashMap<>();
    private Map<String, SessionFactory> selectedSessionFactories = new HashMap<>();

    public void beforeStart(@Nullable UnitOfWork unitOfWork) {
        if (unitOfWork == null) {
            return;
        }
        this.unitOfWork = unitOfWork;

        selectedSessionFactories = sessionFactories.entrySet()
            .stream()
            .filter(sessionFactoryEntry -> Arrays.asList(unitOfWork.value()).contains(sessionFactoryEntry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        if (selectedSessionFactories.isEmpty()
            || selectedSessionFactories.size() != unitOfWork.value().length) {

            // If the user didn't specify the name of a session factory,
            // and we have only one registered, we can assume that it's the right one.
            if (unitOfWork.value().length == 1
                && unitOfWork.value()[0].equals(HibernateBundle.DEFAULT_NAME)
                && sessionFactories.size() == 1) {

                selectedSessionFactories = new HashMap<>();
                selectedSessionFactories.putAll(sessionFactories);
            } else {
                List<String> unregistered = Arrays.asList(unitOfWork.value()).stream()
                    .filter(name -> !sessionFactories.containsKey(name))
                    .collect(Collectors.toList());

                throw new IllegalArgumentException("Unregistered Hibernate bundle/s: '" + String.join("','", unregistered) + "'");
            }
        }

        sessions = selectedSessionFactories.entrySet().stream()
            .map(entry -> Pair.of(entry.getKey(), entry.getValue().openSession()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        try {
            configureSessions();
            sessions.values().forEach(ManagedSessionContext::bind);
            beginTransactions(unitOfWork, sessions);
        } catch (Throwable th) {
            onFinish();
            throw th;
        }
    }

    public void afterEnd() {
        if (unitOfWork == null || sessions.isEmpty()) {
            return;
        }

        try {
            commitTransactions(unitOfWork, sessions);
        } catch (Exception e) {
            rollbackTransactions(unitOfWork, sessions);
            throw e;
        }
        // We should not close the sessions to let the lazy loading work during serializing a response to the client.
        // If the response successfully serialized, then the sessions will be closed by the `onFinish` method
    }

    public void onError() {
        if (unitOfWork == null || sessions.isEmpty()) {
            return;
        }

        try {
            rollbackTransactions(unitOfWork, sessions);
        } finally {
            onFinish();
        }
    }

    public void onFinish() {
        try {
            //We need to ensure that all the sessions have been closed
            //and also we need to inform about the exceptions
            List<Optional<RuntimeException>> errors = sessions.values().stream()
                .map(UnitOfWorkAspect::closeSession)
                .filter(Optional::isPresent)
                .collect(Collectors.toList());

            if (!errors.isEmpty()) {
                throw errors.iterator().next().get();
            }
        } finally {
            sessions = new HashMap<>();
            selectedSessionFactories.values().forEach(ManagedSessionContext::unbind);
        }
    }

    private static Optional<RuntimeException> closeSession(Session session) {
        if (session != null) {
            try {
                session.close();
            } catch (HibernateException e) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    protected void configureSessions() {
        sessions.values().stream().forEach(session -> {
            checkNotNull(unitOfWork);
            checkNotNull(session);
            session.setDefaultReadOnly(unitOfWork.readOnly());
            session.setCacheMode(unitOfWork.cacheMode());
            session.setHibernateFlushMode(unitOfWork.flushMode());
        });
    }

    private void beginTransactions(UnitOfWork unitOfWork, Map<String, Session> sessions) {
        if (!unitOfWork.transactional()) {
            return;
        }
        sessions.values().forEach(Session::beginTransaction);
    }

    private static Optional<RuntimeException> rollbackTransaction(Session session) {
        try {
            final Transaction txn = session.getTransaction();
            if (txn != null && txn.getStatus().canRollback()) {
                txn.rollback();
            }
            return Optional.empty();
        } catch (RuntimeException e) {
            return Optional.of(e);
        }
    }

    private void rollbackTransactions(UnitOfWork unitOfWork, Map<String, Session> sessions) {
        if (!unitOfWork.transactional()) {
            return;
        }

        //We need to ensure that the changes in all the sessions have been rollbacked
        //and also we need to inform about the exceptions
        List<Optional<RuntimeException>> errors = sessions.values().stream()
            .map(UnitOfWorkAspect::rollbackTransaction)
            .filter(Optional::isPresent)
            .collect(Collectors.toList());

        if (!errors.isEmpty()) {
            throw errors.iterator().next().get();
        }
    }

    private void commitTransactions(UnitOfWork unitOfWork, Map<String, Session> sessions) {
        if (!unitOfWork.transactional()) {
            return;
        }

        sessions.values().stream()
            .map(Session::getTransaction)
            .filter(txn -> (txn != null && txn.getStatus().canRollback()))
            .forEach(Transaction::commit);
    }

    protected Session getSession() {
        checkState(sessions.size() == 1);

        // If the user didn't specify the name of a session,
        // and we have only one selected, we can assume that it's the right one.
        return requireNonNull(sessions.values().iterator().next());
    }

    protected Session getSession(String name) {
        return requireNonNull(sessions.get(name));
    }

    protected SessionFactory getSessionFactory() {
        checkState(selectedSessionFactories.size() == 1);

        // If the user didn't specify the name of a session factories,
        // and we have only one selected, we can assume that it's the right one.
        return requireNonNull(selectedSessionFactories.values().iterator().next());
    }

    protected SessionFactory getSessionFactory(String name) {
        return requireNonNull(selectedSessionFactories.get(name));
    }

}
