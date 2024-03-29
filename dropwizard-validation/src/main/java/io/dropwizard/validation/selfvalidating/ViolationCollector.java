package io.dropwizard.validation.selfvalidating;

import jakarta.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import java.util.Collections;
import java.util.Map;

import static io.dropwizard.validation.InterpolationHelper.escapeMessageParameter;

/**
 * This class is a simple wrapper around the ConstraintValidatorContext of hibernate validation.
 * It collects all the violations of the SelfValidation methods of an object.
 */
public class ViolationCollector {
    private final ConstraintValidatorContext constraintValidatorContext;
    private boolean violationOccurred = false;

    /**
     * Constructs a new {@link ViolationCollector} with the given {@link ConstraintValidatorContext}.
     *
     * @param constraintValidatorContext the wrapped {@link ConstraintValidatorContext}
     */
    public ViolationCollector(ConstraintValidatorContext constraintValidatorContext) {
        this.constraintValidatorContext = constraintValidatorContext;
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     * <br/>
     * Prefer the method with explicit message parameters if you want to interpolate the message.
     *
     * @param message the message of the violation
     * @see #addViolation(String, Map)
     */
    public void addViolation(String message) {
        addViolation(message, Collections.emptyMap());
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param message           the message of the violation
     * @param messageParameters a map of message parameters which can be interpolated in the violation message
     * @since 2.0.3
     */
    public void addViolation(String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        getContextWithMessageParameters(messageParameters)
                .buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     * <br/>
     * Prefer the method with explicit message parameters if you want to interpolate the message.
     *
     * @param propertyName the name of the property
     * @param message      the message of the violation
     * @see #addViolation(String, String, Map)
     * @since 2.0.2
     */
    public void addViolation(String propertyName, String message) {
        addViolation(propertyName, message, Collections.emptyMap());
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName      the name of the property
     * @param message           the message of the violation
     * @param messageParameters a map of message parameters which can be interpolated in the violation message
     * @since 2.0.3
     */
    public void addViolation(String propertyName, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        getContextWithMessageParameters(messageParameters)
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     * Prefer the method with explicit message parameters if you want to interpolate the message.
     *
     * @param propertyName the name of the property with the violation
     * @param index        the index of the element with the violation
     * @param message      the message of the violation (any EL expression will be escaped and not parsed)
     * @see ViolationCollector#addViolation(String, Integer, String, Map)
     * @since 2.0.2
     */
    public void addViolation(String propertyName, Integer index, String message) {
        addViolation(propertyName, index, message, Collections.emptyMap());
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName      the name of the property with the violation
     * @param index             the index of the element with the violation
     * @param message           the message of the violation
     * @param messageParameters a map of message parameters which can be interpolated in the violation message
     * @since 2.0.3
     */
    public void addViolation(String propertyName, Integer index, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        getContextWithMessageParameters(messageParameters)
                .buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addBeanNode().inIterable().atIndex(index)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName the name of the property with the violation
     * @param key          the key of the element with the violation
     * @param message      the message of the violation
     * @since 2.0.2
     */
    public void addViolation(String propertyName, String key, String message) {
        addViolation(propertyName, key, message, Collections.emptyMap());
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName      the name of the property with the violation
     * @param key               the key of the element with the violation
     * @param message           the message of the violation
     * @param messageParameters a map of message parameters which can be interpolated in the violation message
     * @since 2.0.3
     */
    public void addViolation(String propertyName, String key, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        final HibernateConstraintValidatorContext context = getContextWithMessageParameters(messageParameters);
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(propertyName)
                .addBeanNode().inIterable().atKey(key)
                .addConstraintViolation();
    }

    /**
     * Returns a {@link HibernateConstraintValidatorContext} updated with the given message parameters.
     *
     * @param messageParameters the message parameters to set to the context
     * @return the {@link HibernateConstraintValidatorContext}
     */
    private HibernateConstraintValidatorContext getContextWithMessageParameters(Map<String, Object> messageParameters) {
        final HibernateConstraintValidatorContext context =
                constraintValidatorContext.unwrap(HibernateConstraintValidatorContext.class);
        for (Map.Entry<String, Object> messageParameter : messageParameters.entrySet()) {
            final Object value = messageParameter.getValue();
            final String escapedValue = value == null ? null : escapeMessageParameter(value.toString());
            context.addMessageParameter(messageParameter.getKey(), escapedValue);
        }
        return context;
    }

    /**
     * This method returns the wrapped context for raw access to the validation framework. If you use
     * the context to add violations make sure to call <code>setViolationOccurred(true)</code>.
     *
     * @return the wrapped Hibernate ConstraintValidatorContext
     */
    public ConstraintValidatorContext getContext() {
        return constraintValidatorContext;
    }

    /**
     * Returns, if a violation has previously occurred.
     *
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
