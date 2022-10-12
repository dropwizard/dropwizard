package io.dropwizard.validation.selfvalidating;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated element has methods annotated by
 * {@link io.dropwizard.validation.selfvalidating.SelfValidation}.
 * Those methods are executed on validation.
 */
@Documented
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SelfValidatingValidator.class)
public @interface SelfValidating {

    /**
     * The validation message for this constraint.
     *
     * @return the message
     */
    String message() default "";

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
