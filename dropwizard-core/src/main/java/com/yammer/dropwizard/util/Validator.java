package com.yammer.dropwizard.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static java.lang.String.format;

/**
 * A simple façade for Hibernate Validator.
 */
public class Validator {
    private static final ValidatorFactory FACTORY = Validation.buildDefaultValidatorFactory();

    /**
     * Validates the given object, and returns a list of error messages, if any. If the returned
     * list is empty, the object is valid.
     *
     * @param o a potentially-valid object
     * @param <T> the type of object to validate
     * @return a list of error messages, if any, regarding {@code o}'s validity
     */
    public <T> ImmutableList<String> validate(T o) {
        final Set<String> errors = Sets.newHashSet();
        final Set<ConstraintViolation<T>> violations = FACTORY.getValidator().validate(o);
        for (ConstraintViolation<T> v : violations) {
            errors.add(format("%s %s (was %s)", v.getPropertyPath(),
                                                v.getMessage(),
                                                v.getInvalidValue()));
        }
        return ImmutableList.copyOf(Ordering.natural().sortedCopy(errors));
    }
}
