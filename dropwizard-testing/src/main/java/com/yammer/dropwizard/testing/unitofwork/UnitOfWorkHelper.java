package com.yammer.dropwizard.testing.unitofwork;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.SessionFactoryFactory;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.context.internal.ManagedSessionContext;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static com.yammer.dropwizard.testing.JsonHelpers.fromJson;
import static com.yammer.dropwizard.testing.JsonHelpers.jsonFixture;

public class UnitOfWorkHelper {

    Session session;
    static SessionFactory hibernateSessionFactory;
    final static ObjectMapper MAPPER = new ObjectMapperFactory().build();
    final static String DATABASE = "database";

    public SessionFactory getSessionFactory() {
        return hibernateSessionFactory;
    }

    protected static void initDB(String configFile, Class<?>[] entities)throws IOException, ClassNotFoundException {

        JsonNode jsonNode = MAPPER.readTree(jsonFixture(configFile)).get(DATABASE);
        DatabaseConfiguration config = fromJson(jsonNode.toString(), DatabaseConfiguration.class);
        hibernateSessionFactory = createHibernateSessionFactory(config, entities);
    }

    protected void startSession() {
        session = openSession(hibernateSessionFactory);
        session.beginTransaction();
    }

    protected Session openSession(SessionFactory hibernateSessionFactory) {
        Session session = hibernateSessionFactory.openSession();
        session.setDefaultReadOnly(true);
        session.setFlushMode(FlushMode.ALWAYS);
        ManagedSessionContext.bind(session);
        return session;
    }

    protected static SessionFactory createHibernateSessionFactory(DatabaseConfiguration dbConfig, Class<?>[] entities) throws IOException, ClassNotFoundException {
        SessionFactoryFactory factory = new SessionFactoryFactory();
        Environment mockEnvironment = Mockito.mock(Environment.class);
        return factory.build(mockEnvironment, dbConfig, asList(entities));
    }

    public static List<Class<?>> asList(Class<?>... classes) {
        return Lists.newArrayList(classes);
    }

    protected static <T> Set<T> asSet(T... set) {
        return Sets.newHashSet(set);
    }

    protected void rollbackAndCloseSession() {
        ManagedSessionContext.unbind(hibernateSessionFactory);
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.isActive()) {
            txn.rollback();
        }
        session.close();
    }

    protected void commitAndCloseSession() {
        ManagedSessionContext.unbind(hibernateSessionFactory);
        commitTransaction();
        session.close();
    }

    private void commitTransaction() {
        final Transaction txn = session.getTransaction();
        if (txn != null && txn.isActive()) {
            txn.commit();
        }
    }
}

