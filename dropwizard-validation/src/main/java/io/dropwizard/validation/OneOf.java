package io.dropwizard.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Checks to see that the value is one of a set of elements.
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = OneOfValidator.class)
public @interface OneOf {
    /**
     * The validation message for this constraint.
     *
     * @return the message
     */
    String message() default "must be one of {value}";

    /**
     * The groups the constraint belongs to.
     *
     * @return an array of classes representing the groups
     */
    Class<?>[] groups() default {};

    /**
     * The payloads of this constraint.
     *
     * @return the array of payload classes
     */
    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default {};

    /**
     * The set of valid values.
     *
     * @return an array containing the valid string values
     */
    String[] value();

    /**
     * Whether to ignore case.
     *
     * @return if the case should be ignored
     */
    boolean ignoreCase() default false;

    /**
     * Whether to ignore leading and trailing whitespace.
     *
     * @return if leading and trailing whitespaces should be ignored
     */
    boolean ignoreWhitespace() default false;
}
