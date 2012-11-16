package com.yammer.dropwizard.hibernate;

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

}
