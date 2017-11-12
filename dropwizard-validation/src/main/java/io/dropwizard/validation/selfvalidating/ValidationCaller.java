package io.dropwizard.validation.selfvalidating;

import javax.annotation.Nullable;

/**
 * This class represents a wrapper for calling validation methods annotated with <code>@SelfValidation</code>.
 * It is used as a base class for the code generation.
 *
 * @param <T> the object type that contains the validation method
 */
public abstract class ValidationCaller<T> {

    @Nullable
    protected T validationObject;

    public void setValidationObject(T obj) {
        this.validationObject = obj;
    }

    @Nullable
    public T getValidationObject() {
        return validationObject;
    }

    public abstract void call(ViolationCollector vc);
}
