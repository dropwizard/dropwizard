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
    @OverridesAttribute(constraint = MinDataSize.class, name = "value")
    long min() default 0;

    @OverridesAttribute(constraint = MaxDataSize.class, name = "value")
    long max() default Long.MAX_VALUE;

    @OverridesAttribute(constraint = MinDataSize.class, name = "unit")
    @OverridesAttribute(constraint = MaxDataSize.class, name = "unit")
    DataSizeUnit unit() default DataSizeUnit.BYTES;

    String message() default "must be between {min} {unit} and {max} {unit}";

    Class<?>[] groups() default { };

    @SuppressWarnings("UnusedDeclaration") Class<? extends Payload>[] payload() default { };

    /**
     * Defines several {@code @SizeRange} annotations on the same element.
     */
    @Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER, TYPE_USE })
    @Retention(RUNTIME)
    @Documented
    @interface List {
        DataSizeRange[] value();
    }
}
