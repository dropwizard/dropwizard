package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.util.concurrent.MoreExecutors;
import io.dropwizard.db.TimeBoundHealthCheck;
import io.dropwizard.util.Duration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.concurrent.ExecutorService;

public class SessionFactoryHealthCheck extends HealthCheck {
    private final SessionFactory sessionFactory;
    private final String validationQuery;
    private final TimeBoundHealthCheck timeBoundHealthCheck;

    public SessionFactoryHealthCheck(SessionFactory sessionFactory,
                                     String validationQuery) {
        this(MoreExecutors.newDirectExecutorService(), Duration.seconds(0), sessionFactory, validationQuery);
    }

    public SessionFactoryHealthCheck(ExecutorService executorService,
                                     Duration duration,
                                     SessionFactory sessionFactory,
                                     String validationQuery) {
        this.sessionFactory = sessionFactory;
        this.validationQuery = validationQuery;
        this.timeBoundHealthCheck = new TimeBoundHealthCheck(executorService, duration);
    }
    

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public String getValidationQuery() {
        return validationQuery;
    }

    @Override
    protected Result check() throws Exception {
        return timeBoundHealthCheck.check(() -> {
            final Session session = sessionFactory.openSession();
            try {
                final Transaction txn = session.beginTransaction();
                try {
                    session.createSQLQuery(validationQuery).list();
                    txn.commit();
                } catch (Exception e) {
                    if (txn.getStatus().canRollback()) {
                        txn.rollback();
                    }
                    throw e;
                }
            } finally {
                session.close();
            }
            return Result.healthy();
        });
    }
}
