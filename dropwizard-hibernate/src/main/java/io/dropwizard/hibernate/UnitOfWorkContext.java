package io.dropwizard.hibernate;

import org.hibernate.SessionFactory;

import static java.util.Objects.requireNonNull;

public class UnitOfWorkContext {
    private final UnitOfWork unitOfWork;
    private final SessionFactory sessionFactory;

    UnitOfWorkContext(UnitOfWork unitOfWork,
                      SessionFactory sessionFactory) {
        this.unitOfWork = requireNonNull(unitOfWork);
        this.sessionFactory = requireNonNull(sessionFactory);
    }

    public UnitOfWork getUnitOfWork() {
        return unitOfWork;
    }

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}
