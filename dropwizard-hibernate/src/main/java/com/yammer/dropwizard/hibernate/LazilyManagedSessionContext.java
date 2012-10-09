package com.yammer.dropwizard.hibernate;

import org.hibernate.Session;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;

public class LazilyManagedSessionContext extends ManagedSessionContext {
    private static final long serialVersionUID = 2777676604946300892L;

    public LazilyManagedSessionContext(SessionFactoryImplementor factory) {
        super(factory);
    }

    @Override
    @SuppressWarnings("HibernateResourceOpenedButNotSafelyClosed")
    public Session currentSession() {
        final SessionFactoryImplementor factory = factory();
        if (!hasBind(factory)) {
            final Session session = factory.openSession();
            session.getTransaction().begin();
            bind(session);
        }
        return super.currentSession();
    }
}
