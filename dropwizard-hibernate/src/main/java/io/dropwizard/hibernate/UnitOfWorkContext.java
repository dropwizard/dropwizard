package io.dropwizard.hibernate;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.annotation.Nullable;

public class UnitOfWorkContext {
    private static final ThreadLocal<UnitOfWork> UNIT_OF_WORK = new ThreadLocal<>();
    private static final ThreadLocal<SessionFactory> SESSION_FACTORY = new ThreadLocal<>();

    public static void setUnitOfWork(UnitOfWork unitOfWork) {
        UNIT_OF_WORK.set(unitOfWork);
    }

    @Nullable
    public static UnitOfWork getUnitOfWork() {
        return UNIT_OF_WORK.get();
    }

    public static void setSessionFactory(SessionFactory sessionFactory) {
        SESSION_FACTORY.set(sessionFactory);
    }

    @Nullable
    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY.get();
    }

    public static Session getCurrentSession() {
        SessionFactory sessionFactory = getSessionFactory();
        if (sessionFactory == null) {
            clear();
            throw new RuntimeException("No session factory set");
        }
        return sessionFactory.getCurrentSession();
    }

    public static void clear() {
        UNIT_OF_WORK.remove();
        SESSION_FACTORY.remove();
    }
}
