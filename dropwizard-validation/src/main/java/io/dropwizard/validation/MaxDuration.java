package io.dropwizard.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
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
 * The annotated element must be a {@link io.dropwizard.util.Duration}
 * whose value must be higher or equal to the specified minimum.
 * <p/>
 * <code>null</code> elements are considered valid
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MaxDurationValidator.class)
public @interface MaxDuration {
    String message() default "must be less than (or equal to, if in 'inclusive' mode) {value} {unit}";

    Class<?>[] groups() default { };

    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default { };

    /**
     * @return value the element must be higher or equal to
     */
    long value();

    /**
     * @return unit of the value the element must be higher or equal to
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * @return {@code true} if the validation is to allow values equal to {@link #value()}.
     * False if the validation is to be exclusive.
     * Defaults to {@code true}.
     */
    boolean inclusive() default true;
}
