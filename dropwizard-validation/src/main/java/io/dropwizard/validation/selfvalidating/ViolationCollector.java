package io.dropwizard.validation.selfvalidating;

import javax.validation.ConstraintValidatorContext;

/**
 * This class is a simple wrapper around the ConstraintValidatorContext of hibernate validation.
 * It collects all the violations of the SelfValidation methods of an object.
 */
public class ViolationCollector {

    private boolean violationOccurred = false;
    private ConstraintValidatorContext context;


    public ViolationCollector(ConstraintValidatorContext context) {
        this.context = context;
    }

    /**
     * Adds a new violation to this collector. This also sets violationOccurred to true.
     *
     * @param msg the message of the violation
     */
    public void addViolation(String msg) {
        violationOccurred = true;
        context.buildConstraintViolationWithTemplate(msg)
            .addConstraintViolation();
    }

    /**
     * This method returns the wrapped context for raw access to the validation framework. If you use
     * the context to add violations make sure to call <code>setViolationOccurred(true)</code>.
     *
     * @return the wrapped Hibernate ConstraintValidatorContext
     */
    public ConstraintValidatorContext getContext() {
        return context;
    }

    /**
     * @return if any violation was collected
     */
    public boolean hasViolationOccurred() {
        return violationOccurred;
    }

    /**
     * Manually sets if a violation occurred. This is automatically set if <code>addViolation</code> is called.
     *
     * @param violationOccurred if any violation was collected
     */
    public void setViolationOccurred(boolean violationOccurred) {
        this.violationOccurred = violationOccurred;
    }

}
