package io.dropwizard.hibernate;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Container annotation for repeated {@link UnitOfWork} annotations.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
@interface UnitsOfWork {
    UnitOfWork[] value();
}
