package io.dropwizard.validation;

import io.dropwizard.util.SizeUnit;

import javax.validation.Constraint;
import javax.validation.Payload;
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
 * The annotated element must be a {@link io.dropwizard.util.Size}
 * whose value must be higher or equal to the specified minimum.
 * <br/>
 * <code>null</code> elements are considered valid
 *
 * @deprecated Use {@link MinDataSize} for correct SI and IEC prefixes.
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Deprecated
@Documented
@Constraint(validatedBy = MinSizeValidator.class)
public @interface MinSize {
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
    Class<?>[] groups() default { };

    /**
     * The payloads of this constraint.
     *
     * @return the array of payload classes
     */
    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default { };

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
    SizeUnit unit() default SizeUnit.BYTES;
}
