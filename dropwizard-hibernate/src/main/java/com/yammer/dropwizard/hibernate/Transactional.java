package com.yammer.dropwizard.hibernate;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * When annotating a Jersey resource method, wraps the method in a Hibernate session and
 * transaction. If the resource method returns without throwing an exception, the transaction is
 * committed and the session is closed. If the resource method throws an exception, the transaction
 * is rolled back and the session is closed.
 *
 * @see TransactionalRequestDispatcher
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface Transactional {
    /**
     * If {@code true}, the Hibernate session will default to loading read-only entities.
     *
     * @see org.hibernate.Session#setDefaultReadOnly(boolean)
     */
    boolean readOnly() default false;

    /**
     * The {@link CacheMode} for the session.
     *
     * @see CacheMode
     * @see org.hibernate.Session#setCacheMode(CacheMode)
     */
    CacheMode cacheMode() default CacheMode.NORMAL;

    /**
     * The {@link FlushMode} for the session.
     *
     * @see FlushMode
     * @see org.hibernate.Session#setFlushMode(org.hibernate.FlushMode)
     */
    FlushMode flushMode() default FlushMode.AUTO;
}
