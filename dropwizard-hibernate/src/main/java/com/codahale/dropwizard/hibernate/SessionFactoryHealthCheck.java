package com.codahale.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheck;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class SessionFactoryHealthCheck extends HealthCheck {
    private final SessionFactory sessionFactory;
    private final String validationQuery;

    public SessionFactoryHealthCheck(SessionFactory sessionFactory,
                                     String validationQuery) {
        this.sessionFactory = sessionFactory;
        this.validationQuery = validationQuery;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    @Override
    protected Result check() throws Exception {
        final Session session = sessionFactory.openSession();
        try {
            final Transaction txn = session.beginTransaction();
            try {
                session.createSQLQuery(validationQuery).list();
                txn.commit();
            } catch (Exception e) {
                if (txn.isActive()) {
                    txn.rollback();
                }
                throw e;
            }
        } finally {
            session.close();
        }
        return Result.healthy();
    }
}
