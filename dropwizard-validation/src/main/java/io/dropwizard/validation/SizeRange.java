package io.dropwizard.validation;

import io.dropwizard.util.SizeUnit;

import javax.validation.Constraint;
import javax.validation.OverridesAttribute;
import javax.validation.Payload;
import javax.validation.ReportAsSingleViolation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotated element has to be in the appropriate range. Apply on
 * {@link io.dropwizard.util.Size} instances.
 */
@Documented
@Constraint(validatedBy = { })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@MinSize(0)
@MaxSize(value = Long.MAX_VALUE, unit = SizeUnit.TERABYTES)
@ReportAsSingleViolation
public @interface SizeRange {
    @OverridesAttribute(constraint = MinSize.class, name = "value")
    long min() default 0;

    @OverridesAttribute(constraint = MaxSize.class, name = "value")
    long max() default Long.MAX_VALUE;

    @OverridesAttribute.List({
        @OverridesAttribute(constraint = MinSize.class, name = "unit"),
        @OverridesAttribute(constraint = MaxSize.class, name = "unit")
    })
    SizeUnit unit() default SizeUnit.BYTES;

    String message() default "must be between {min} {unit} and {max} {unit}";

    Class<?>[] groups() default { };

    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default { };

    /**
     * Defines several {@code @SizeRange} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
    @Retention(RUNTIME)
    @Documented
    public @interface List {
        SizeRange[] value();
    }
}
