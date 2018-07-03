package io.dropwizard.hibernate;

import org.hibernate.SessionFactory;

public class ClusteredSessionFactory {
    private final ThreadLocal<Boolean> readOnly = new ThreadLocal<>();
    private final SessionFactory writeSessionFactory;
    private final SessionFactory readSessionFactory;

    public ClusteredSessionFactory(SessionFactory writeSessionFactory, SessionFactory readSessionFactory) {
        this.writeSessionFactory = writeSessionFactory;
        this.readSessionFactory = readSessionFactory;
    }

    public void setReadOnly(Boolean readOnlyValue) {
        readOnly.set(readOnlyValue);
    }

    public SessionFactory getSessionFactory() {
        if (readOnly.get() != null && readOnly.get()) {
            return readSessionFactory;
        }

        return writeSessionFactory;
    }
}
