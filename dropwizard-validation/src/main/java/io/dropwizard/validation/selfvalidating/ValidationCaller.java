package io.dropwizard.validation.selfvalidating;

import org.jspecify.annotations.Nullable;

/**
 * This class represents a wrapper for calling validation methods annotated with <code>@SelfValidation</code>.
 * It is used as a base class for the code generation.
 *
 * @param <T> the object type that contains the validation method
 */
public abstract class ValidationCaller<T> {

    /**
     * The object to call validation methods on.
     */
    @Nullable
    protected T validationObject;

    /**
     * Sets the validation object variable.
     *
     * @param obj the new validation object
     */
    public void setValidationObject(T obj) {
        this.validationObject = obj;
    }

    /**
     * Gets the validation object.
     *
     * @return the validation object
     */
    @Nullable
    public T getValidationObject() {
        return validationObject;
    }

    /**
     * This method is intended to call a validation methods on the validation object.
     *
     * @param vc the {@link ViolationCollector} to collect violations
     */
    public abstract void call(ViolationCollector vc);
}
