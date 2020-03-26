package io.dropwizard.validation.selfvalidating;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

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

    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * Escape EL expressions to avoid template injection attacks.
     * <p>
     * This has serious security implications and you will
     * have to escape the violation messages added to {@link ViolationCollector} appropriately.
     *
     * @see ViolationCollector#addViolation(String, Map)
     * @see ViolationCollector#addViolation(String, String, Map)
     * @see ViolationCollector#addViolation(String, Integer, String, Map)
     */
    boolean escapeExpressions() default true;
}
