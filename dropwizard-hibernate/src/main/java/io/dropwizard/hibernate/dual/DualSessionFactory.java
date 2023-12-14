package io.dropwizard.hibernate.dual;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import jakarta.persistence.Query;
import jakarta.persistence.SynchronizationType;
import jakarta.persistence.metamodel.Metamodel;
import org.hibernate.Cache;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.graph.RootGraph;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.hibernate.relational.SchemaManager;
import org.hibernate.stat.Statistics;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

/** Represents a wrapper/decorator class for a Hibernate session factory that can manage
 *  both a primary session factory and a read-only session factory.
 *
 * @since 2.1
 *
 */

@SuppressWarnings({"rawtypes"})
public class DualSessionFactory implements SessionFactory {

    private static final long serialVersionUID = 1L;

    private final SessionFactory primary;
    private final SessionFactory reader;
    private final ThreadLocal<SessionFactory> current = new ThreadLocal<SessionFactory>();

    public DualSessionFactory(final SessionFactory primary, final SessionFactory reader) {
        this.primary = primary;
        this.reader = reader;
        this.current.set(primary);    // Main thread should use primary.
    }

    /** Activates either the primary or the reader session factory depending on the readOnly parameter.
     *
     * @param readOnly
     * @return the session factory in use
     */
    public SessionFactory prepare(final boolean readOnly) {
        final SessionFactory factory = readOnly ? reader : primary;
        current.set(factory);

        return factory;
    }

    public SessionFactory current() { return current.get(); }

    @Override
    public EntityManager createEntityManager() { return current().createEntityManager(); }

    @Override
    public EntityManager createEntityManager(final Map map) { return current().createEntityManager(map); }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType) { return current().createEntityManager(synchronizationType); }

    @Override
    public EntityManager createEntityManager(final SynchronizationType synchronizationType, final Map map) { return current().createEntityManager(synchronizationType, map); }

    @Override
    public HibernateCriteriaBuilder getCriteriaBuilder() {
        return current().getCriteriaBuilder();
    }

    @Override
    public Metamodel getMetamodel() {
        return current().getMetamodel();
    }

    @Override
    public boolean isOpen() { return current().isOpen(); }

    @Override
    public Map<String, Object> getProperties() { return current().getProperties(); }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() { return current().getPersistenceUnitUtil(); }

    @Override
    public void addNamedQuery(String name, Query query) { current().addNamedQuery(name, query); }

    @Override
    public <T> T unwrap(Class<T> cls) { return current().unwrap(cls); }

    @Override
    public <T> void addNamedEntityGraph(String graphName, EntityGraph<T> entityGraph) { current().addNamedEntityGraph(graphName, entityGraph); }

    @Override
    public <T> List<EntityGraph<? super T>> findEntityGraphsByType(Class<T> entityClass) { return current().findEntityGraphsByType(entityClass); }

    @Override
    public Reference getReference() throws NamingException { return current().getReference(); }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() { return current().getSessionFactoryOptions(); }

    @Override
    public SessionBuilder withOptions() { return current().withOptions(); }

    @Override
    public Session openSession() throws HibernateException { return current().openSession(); }

    @Override
    public Session getCurrentSession() throws HibernateException { return current().getCurrentSession(); }

    @Override
    public StatelessSessionBuilder withStatelessOptions() { return current().withStatelessOptions(); }

    @Override
    public StatelessSession openStatelessSession() { return current().openStatelessSession(); }

    @Override
    public StatelessSession openStatelessSession(Connection connection) { return current().openStatelessSession(connection); }

    @Override
    public void inSession(Consumer<Session> action) {
        current().inSession(action);
    }

    @Override
    public void inTransaction(Consumer<Session> action) {
        current().inTransaction(action);
    }

    @Override
    public <R> R fromSession(Function<Session, R> action) {
        return current().fromSession(action);
    }

    @Override
    public <R> R fromTransaction(Function<Session, R> action) {
        return current().fromTransaction(action);
    }

    @Override
    public Statistics getStatistics() { return current().getStatistics(); }

    @Override
    public void close() throws HibernateException { current().close(); }

    @Override
    public boolean isClosed() { return current().isClosed(); }

    @Override
    public Cache getCache() { return current().getCache(); }

    @Override
    public Set getDefinedFilterNames() { return current().getDefinedFilterNames(); }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException { return current().getFilterDefinition(filterName); }

    @Override
    public boolean containsFetchProfileDefinition(String name) { return current().containsFetchProfileDefinition(name); }

    @Override
    public SchemaManager getSchemaManager() {
        return current().getSchemaManager();
    }

    @Override
    public RootGraph<?> findEntityGraphByName(String s) {
        return current().findEntityGraphByName(s);
    }

    @Override
    public Set<String> getDefinedFetchProfileNames() {
        return current().getDefinedFetchProfileNames();
    }
}
