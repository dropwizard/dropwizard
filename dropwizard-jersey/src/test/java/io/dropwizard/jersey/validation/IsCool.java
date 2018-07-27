package io.dropwizard.jersey.validation;

import javax.annotation.Nullable;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({ METHOD, FIELD, PARAMETER })
@Retention(RUNTIME)
@Constraint(validatedBy = IsCool.CoolValidator.class)
public @interface IsCool {

    String message() default "is not yet cool enough (or maybe it's too cool)!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class CoolValidator implements ConstraintValidator<IsCool, String> {
        @Context
        @Nullable
        private UriInfo uriInfo;

        @Override
        public void initialize(IsCool constraintAnnotation) {
        }

        @Override
        public boolean isValid(String value, ConstraintValidatorContext context) {
            return value != null && value.endsWith("iscool")
                && uriInfo != null && uriInfo.getQueryParameters().containsKey("sneaky-param");
        }
    }
}
