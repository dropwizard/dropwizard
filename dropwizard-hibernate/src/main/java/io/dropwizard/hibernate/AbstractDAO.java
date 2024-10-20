package io.dropwizard.hibernate;

import io.dropwizard.util.Generics;
import jakarta.persistence.criteria.CriteriaQuery;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * An abstract base class for Hibernate DAO classes.
 *
 * @param <E> the class which this DAO manages
 */
public class AbstractDAO<E> {
    private final SessionFactory sessionFactory;
    private final Class<?> entityClass;

    /**
     * Creates a new DAO with a given session provider.
     *
     * @param sessionFactory    a session provider
     */
    public AbstractDAO(SessionFactory sessionFactory) {
        this.sessionFactory = requireNonNull(sessionFactory);
        this.entityClass = Generics.getTypeParameter(getClass());
    }

    /**
     * Returns the current {@link Session}.
     *
     * @return the current session
     */
    protected Session currentSession() {
        return sessionFactory.getCurrentSession();
    }

    /**
     * Creates a new {@link CriteriaQuery} for {@code <E>}.
     *
     * @return a new {@link CriteriaQuery} query
     */
    protected CriteriaQuery<E> criteriaQuery() {
        return this.currentSession().getCriteriaBuilder().createQuery(getEntityClass());
    }

    /**
     * Returns a named {@link Query}.
     *
     * @param queryName the name of the query
     * @return the named query
     * @see Session#getNamedQuery(String)
     */
    protected Query<?> namedQuery(String queryName) throws HibernateException {
        return currentSession().getNamedQuery(requireNonNull(queryName));
    }

    /**
     * Returns a named and type-safe {@link Query}.
     *
     * @param queryName the name of the query
     * @return the named query
     * @see Session#createNamedQuery(String, Class)
     * @since 2.0.22
     */
    protected Query<E> namedTypedQuery(String queryName) throws HibernateException {
        return currentSession().createNamedQuery(queryName, getEntityClass());
    }

    /**
     * Returns a typed {@link Query<E>}
     *
     * @param queryString HQL query
     * @return typed query
     */
    protected Query<E> query(String queryString) {
        return currentSession().createQuery(requireNonNull(queryString), getEntityClass());
    }

    /**
     * Returns the entity class managed by this DAO.
     *
     * @return the entity class managed by this DAO
     */
    @SuppressWarnings("unchecked")
    public Class<E> getEntityClass() {
        return (Class<E>) entityClass;
    }

    /**
     * Convenience method to return a single instance that matches the criteria query,
     * or null if the criteria returns no results.
     *
     * @param criteriaQuery the {@link CriteriaQuery} query to run
     * @return the single result or {@code null}
     * @throws HibernateException if there is more than one matching result
     */
    protected @Nullable E uniqueResult(CriteriaQuery<E> criteriaQuery) throws HibernateException {
        return uniqueElement(
            currentSession()
                .createQuery(requireNonNull(criteriaQuery))
                .getResultList()
        );
    }

    private static <T> @Nullable T uniqueElement(List<T> list) throws NonUniqueResultException {
        if (list.isEmpty()) {
            return null;
        }
        final T head = list.get(0);
        if (list.stream().anyMatch(element -> element != head)) {
            throw new NonUniqueResultException(list.size());
        }
        return head;
    }

    /**
     * Convenience method to return a single instance that matches the query, or null if the query
     * returns no results.
     *
     * @param query the query to run
     * @return the single result or {@code null}
     * @throws HibernateException if there is more than one matching result
     * @see Query#uniqueResult()
     */
    protected E uniqueResult(Query<E> query) throws HibernateException {
        return requireNonNull(query).uniqueResult();
    }

    /**
     * Get the results of a {@link CriteriaQuery} query.
     *
     * @param criteria the {@link CriteriaQuery} query to run
     * @return the list of matched query results
     */
    protected List<E> list(CriteriaQuery<E> criteria) throws HibernateException {
        return currentSession().createQuery(requireNonNull(criteria)).getResultList();
    }

    /**
     * Get the results of a query.
     *
     * @param query the query to run
     * @return the list of matched query results
     * @see Query#list()
     */
    protected List<E> list(Query<E> query) throws HibernateException {
        return requireNonNull(query).list();
    }

    /**
     * Return the persistent instance of {@code <E>} with the given identifier, or {@code null} if
     * there is no such persistent instance. (If the instance, or a proxy for the instance, is
     * already associated with the session, return that instance or proxy.)
     *
     * @param id an identifier
     * @return a persistent instance or {@code null}
     * @throws HibernateException
     * @see Session#get(Class, Object)
     */
    @SuppressWarnings("unchecked")
    protected E get(Object id) {
        return (E) currentSession().get(entityClass, requireNonNull(id));
    }

    /**
     * Either save or update the given instance, depending upon resolution of the unsaved-value
     * checks (see the manual for discussion of unsaved-value checking).
     * <p/>
     * This operation cascades to associated instances if the association is mapped with
     * <tt>cascade="save-update"</tt>.
     *
     * @param entity a transient or detached instance containing new or updated state
     * @throws HibernateException
     * @see Session#saveOrUpdate(Object)
     */
    protected E persist(E entity) throws HibernateException {
        currentSession().saveOrUpdate(requireNonNull(entity));
        return entity;
    }

    /**
     * Force initialization of a proxy or persistent collection.
     * <p/>
     * Note: This only ensures initialization of a proxy object or collection;
     * it is not guaranteed that the elements INSIDE the collection will be initialized/materialized.
     *
     * @param proxy a persistable object, proxy, persistent collection or {@code null}
     * @throws HibernateException if we can't initialize the proxy at this time, e.g. the {@link Session} was closed
     */
    protected <T> T initialize(T proxy) throws HibernateException {
        if (!Hibernate.isInitialized(proxy)) {
            Hibernate.initialize(proxy);
        }
        return proxy;
    }
}
