package io.dropwizard.hibernate;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.db.TimeBoundHealthCheck;
import io.dropwizard.util.DirectExecutorService;
import io.dropwizard.util.Duration;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class SessionFactoryHealthCheck extends HealthCheck {
    private final SessionFactory sessionFactory;
    private final Optional<String> validationQuery;
    private final int validationQueryTimeout;
    private final TimeBoundHealthCheck timeBoundHealthCheck;

    public SessionFactoryHealthCheck(SessionFactory sessionFactory,
                                     Optional<String> validationQuery) {
        this(new DirectExecutorService(), Duration.seconds(0), sessionFactory, validationQuery);
    }

    public SessionFactoryHealthCheck(ExecutorService executorService,
                                     Duration duration,
                                     SessionFactory sessionFactory,
                                     Optional<String> validationQuery) {
        this.sessionFactory = sessionFactory;
        this.validationQuery = validationQuery;
        this.validationQueryTimeout = (int) duration.toSeconds();
        this.timeBoundHealthCheck = new TimeBoundHealthCheck(executorService, duration);
    }


    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public Optional<String> getValidationQuery() {
        return validationQuery;
    }

    @Override
    protected Result check() throws Exception {
        return timeBoundHealthCheck.check(() -> {
            HealthCheck.Result result = Result.healthy();
            try (Session session = sessionFactory.openSession()) {
                final Transaction txn = session.beginTransaction();
                try {
                    if (validationQuery.isPresent()) {
                        session.createNativeQuery(validationQuery.get()).list();
                    } else if (!isValidConnection(session)){
                        result = Result.unhealthy("Connection::isValid returned false.");
                    }
                    txn.commit();
                } catch (Exception e) {
                    if (txn.getStatus().canRollback()) {
                        txn.rollback();
                    }
                    throw e;
                }
            }
            return result;
        });
    }

    private Boolean isValidConnection(Session session) {
        return session.doReturningWork(connection -> connection.isValid(validationQueryTimeout));
    }
}
