package io.dropwizard.validation;

import javax.validation.groups.Default;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Due to limit of @see javax.validation.Valid Annotation for validation groups and ordered validations,
 * this annotation is serving supplementary purposes to validation process.
 */
@Target({PARAMETER, METHOD})
@Retention(RUNTIME)
public @interface Validated {
   /**
    * Specify one or more validation groups to apply to the validation.
    * @return Validation groups
    */
   Class<?>[] value() default {Default.class};
}
