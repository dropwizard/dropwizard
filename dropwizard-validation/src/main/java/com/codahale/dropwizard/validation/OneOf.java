package com.codahale.dropwizard.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks to see that the value is one of a set of elements.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {
    String message() default "must be one of {value}";

    Class<?>[] groups() default {};

    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default {};

    /**
     * The set of valid values.
     */
    String[] value();

    /**
     * Whether or not to ignore case.
     */
    boolean ignoreCase() default false;

    /**
     * Whether or not to ignore leading and trailing whitespace.
     */
    boolean ignoreWhitespace() default false;
}
