package io.dropwizard.validation;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * Validates a bean predicate method as returning true. Bean predicates must be of the form
 * {@code isSomething} or they'll be silently ignored.
 */
@Target({TYPE, ANNOTATION_TYPE, METHOD})
@Retention(RUNTIME)
@Constraint(validatedBy = MethodValidator.class)
@Documented
public @interface ValidationMethod {
    String message() default "is not valid";

    Class<?>[] groups() default {};

    @SuppressWarnings("UnusedDeclaration")
    Class<? extends Payload>[] payload() default {};
}
