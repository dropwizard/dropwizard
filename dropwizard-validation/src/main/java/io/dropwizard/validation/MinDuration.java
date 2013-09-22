package io.dropwizard.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element must be a {@link io.dropwizard.util.Duration}
 * whose value must be higher or equal to the specified minimum.
 * <p/>
 * <code>null</code> elements are considered valid
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = MinDurationValidator.class)
public @interface MinDuration {
    String message() default "must be greater than or equal to {value} {unit}";

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
}
