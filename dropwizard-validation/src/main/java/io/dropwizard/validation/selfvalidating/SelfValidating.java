package io.dropwizard.validation.selfvalidating;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

/**
 * The annotated element has methods annotated by 
 * {@link io.dropwizard.validation.selfvalidating.SelfValidation}.
 * Those methods are executed on validation.
 */
@Documented
@Target({ ElementType.TYPE, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy =  SelfValidatingValidator.class)
public @interface SelfValidating {

    String message() default "";
    
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
