package io.dropwizard.validation.selfvalidating;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This method, if used in conjunction with
 * {@link io.dropwizard.validation.selfvalidating.SelfValidating},
 * will be executed to check if the object itself is valid.
 * For that it requires the signature <code>public void methodName(ViolationCollector)</code>.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SelfValidation {
}
