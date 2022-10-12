package io.dropwizard.validation;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element has to be in the appropriate range. Apply on
 * {@link io.dropwizard.util.Duration} instances.
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@MinDuration(0)
@MaxDuration(value = Long.MAX_VALUE, unit = TimeUnit.DAYS)
@ReportAsSingleViolation
public @interface DurationRange {
    /**
     * The minimum value of the range the validated {@link io.dropwizard.util.Duration} must be in.
     *
     * @return the minimum value
     */
    @OverridesAttribute(constraint = MinDuration.class, name = "value")
    long min() default 0;

    /**
     * The maximum value of the range the validated {@link io.dropwizard.util.Duration} must be in.
     *
     * @return the maximum value
     */
    @OverridesAttribute(constraint = MaxDuration.class, name = "value")
    long max() default Long.MAX_VALUE;

    /**
     * The unit of the validated range.
     *
     * @return the {@link TimeUnit}
     */
    @OverridesAttribute(constraint = MinDuration.class, name = "unit")
    @OverridesAttribute(constraint = MaxDuration.class, name = "unit")
    TimeUnit unit() default TimeUnit.SECONDS;

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
     * Defines several {@code @DurationRange} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        /**
         * The annotation's value.
         *
         * @return the array of {@link DurationRange} annotations this container annotation holds
         */
        DurationRange[] value();
    }
}
