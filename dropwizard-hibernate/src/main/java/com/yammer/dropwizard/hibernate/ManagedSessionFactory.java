package com.yammer.dropwizard.hibernate;

import com.yammer.dropwizard.db.ManagedDataSource;
import com.yammer.dropwizard.lifecycle.Managed;
import org.hibernate.*;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

import javax.naming.NamingException;
import javax.naming.Reference;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

@SuppressWarnings({ "UnusedDeclaration", "rawtypes", "deprecation" })
public class ManagedSessionFactory implements SessionFactory, Managed {
    private static final long serialVersionUID = -8595883229907861848L;

    private final SessionFactory factory;
    private final ManagedDataSource dataSource;

    public ManagedSessionFactory(SessionFactory factory, ManagedDataSource dataSource) {
        this.factory = factory;
        this.dataSource = dataSource;
    }

    @Override
    public SessionFactoryOptions getSessionFactoryOptions() {
        return factory.getSessionFactoryOptions();
    }

    @Override
    public SessionBuilder withOptions() {
        return factory.withOptions();
    }

    @Override
    public Session openSession() throws HibernateException {
        return factory.openSession();
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        return factory.getCurrentSession();
    }

    @Override
    public StatelessSessionBuilder withStatelessOptions() {
        return factory.withStatelessOptions();
    }

    @Override
    public StatelessSession openStatelessSession() {
        return factory.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return factory.openStatelessSession(connection);
    }

    @Override
    public ClassMetadata getClassMetadata(Class entityClass) {
        return factory.getClassMetadata(entityClass);
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName) {
        return factory.getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName) {
        return factory.getCollectionMetadata(roleName);
    }

    @Override
    public Map<String, ClassMetadata> getAllClassMetadata() {
        return factory.getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() {
        return factory.getAllCollectionMetadata();
    }

    @Override
    public Statistics getStatistics() {
        return factory.getStatistics();
    }

    @Override
    public void close() throws HibernateException {
        factory.close();
    }

    @Override
    public boolean isClosed() {
        return factory.isClosed();
    }

    @Override
    public Cache getCache() {
        return factory.getCache();
    }

    @Override
    @Deprecated
    public void evict(Class persistentClass) throws HibernateException {
        factory.evict(persistentClass);
    }

    @Override
    @Deprecated
    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        factory.evict(persistentClass, id);
    }

    @Override
    @Deprecated
    public void evictEntity(String entityName) throws HibernateException {
        factory.evictEntity(entityName);
    }

    @Override
    @Deprecated
    public void evictEntity(String entityName, Serializable id) throws HibernateException {
        factory.evictEntity(entityName, id);
    }

    @Override
    @Deprecated
    public void evictCollection(String roleName) throws HibernateException {
        factory.evictCollection(roleName);
    }

    @Override
    @Deprecated
    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        factory.evictCollection(roleName, id);
    }

    @Override
    @Deprecated
    public void evictQueries(String cacheRegion) throws HibernateException {
        factory.evictQueries(cacheRegion);
    }

    @Override
    @Deprecated
    public void evictQueries() throws HibernateException {
        factory.evictQueries();
    }

    @Override
    public Set getDefinedFilterNames() {
        return factory.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        return factory.getFilterDefinition(filterName);
    }

    @Override
    public boolean containsFetchProfileDefinition(String name) {
        return factory.containsFetchProfileDefinition(name);
    }

    @Override
    public TypeHelper getTypeHelper() {
        return factory.getTypeHelper();
    }

    @Override
    public Reference getReference() throws NamingException {
        return factory.getReference();
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        close();
        dataSource.stop();
    }
}
