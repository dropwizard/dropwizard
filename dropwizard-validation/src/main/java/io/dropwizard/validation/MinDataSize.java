package io.dropwizard.validation;

import io.dropwizard.util.DataSizeUnit;
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
 * The annotated element must be a {@link io.dropwizard.util.DataSize}
 * whose value must be higher or equal to the specified minimum.
 * <br/>
 * <code>null</code> elements are considered valid
 *
 * @since 2.0
 */
@Target({METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MinDataSizeValidator.class)
public @interface MinDataSize {
    /**
     * The validation message for this constraint.
     *
     * @return the message
     */
    String message() default "must be greater than or equal to {value} {unit}";

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
     * The annotation's value.
     *
     * @return value the element must be higher or equal to
     */
    long value();

    /**
     * The unit of the annotation.
     *
     * @return unit of the value the element must be higher or equal to
     */
    DataSizeUnit unit() default DataSizeUnit.BYTES;
}
