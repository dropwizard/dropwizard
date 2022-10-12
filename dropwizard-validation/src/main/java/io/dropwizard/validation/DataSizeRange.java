package io.dropwizard.validation;

import io.dropwizard.util.DataSizeUnit;
import jakarta.validation.Constraint;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.ReportAsSingleViolation;

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
 * The annotated element has to be in the appropriate range.
 * Apply on {@link io.dropwizard.util.DataSize} instances.
 *
 * @since 2.0
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@MinDataSize(0)
@MaxDataSize(value = Long.MAX_VALUE, unit = DataSizeUnit.PEBIBYTES)
@ReportAsSingleViolation
public @interface DataSizeRange {
    /**
     * The minimum value of the range the validated {@link io.dropwizard.util.DataSize} must be in.
     *
     * @return the minimum value
     */
    @OverridesAttribute(constraint = MinDataSize.class, name = "value")
    long min() default 0;

    /**
     * The maximum value of the range the validated {@link io.dropwizard.util.DataSize} must be in.
     *
     * @return the maximum value
     */
    @OverridesAttribute(constraint = MaxDataSize.class, name = "value")
    long max() default Long.MAX_VALUE;

    /**
     * The unit of the validated range.
     *
     * @return the {@link DataSizeUnit}
     */
    @OverridesAttribute(constraint = MinDataSize.class, name = "unit")
    @OverridesAttribute(constraint = MaxDataSize.class, name = "unit")
    DataSizeUnit unit() default DataSizeUnit.BYTES;

    /**
     * The validation message for this constraint.
     *
     * @return the message
     */
    String message() default "must be between {min} {unit} and {max} {unit}";

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
     * Defines several {@code @DataSizeRange} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        /**
         * The annotation's value.
         *
         * @return the array of {@link DataSizeRange} annotations this container annotation holds
         */
        DataSizeRange[] value();
    }
}
