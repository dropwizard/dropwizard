package io.dropwizard.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * A constraint that allows one to specify a port range, but still allow 0 as the port value to
 * indicate dynamically allocated ports.
 *
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, TYPE_USE })
@Retention(RUNTIME)
@Constraint(validatedBy = PortRangeValidator.class)
@Documented
public @interface PortRange {
    /**
     * The minimum value of the port range the validated {@code int} must be in.
     *
     * @return the minimum value
     */
    int min() default 1;

    /**
     * The maximum value of the port range the validated {@code int} must be in.
     *
     * @return the maximum value
     */
    int max() default 65535;

    /**
     * The validation message for this constraint.
     *
     * @return the message
     */
    String message() default "{org.hibernate.validator.constraints.Range.message}";

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
    Class<? extends Payload>[] payload() default {};
}
