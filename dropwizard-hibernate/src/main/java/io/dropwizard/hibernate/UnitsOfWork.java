package io.dropwizard.hibernate;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container annotation for repeated {@link UnitOfWork} annotations.
 */
@Target(METHOD)
@Retention(RUNTIME)
@Documented
public @interface UnitsOfWork {
    UnitOfWork[] value();
}
