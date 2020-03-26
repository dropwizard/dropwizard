package io.dropwizard.validation.selfvalidating;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import java.util.Collections;
import java.util.Map;

import static io.dropwizard.validation.InterpolationHelper.escapeMessageParameter;

/**
 * This class is a simple wrapper around the ConstraintValidatorContext of hibernate validation.
 * It collects all the violations of the SelfValidation methods of an object.
 */
public class ViolationCollector {
    private final ConstraintValidatorContext constraintValidatorContext;
    private final boolean escapeExpressions;

    private boolean violationOccurred = false;

    public ViolationCollector(ConstraintValidatorContext constraintValidatorContext) {
        this(constraintValidatorContext, true);
    }

    public ViolationCollector(ConstraintValidatorContext constraintValidatorContext, boolean escapeExpressions) {
        this.constraintValidatorContext = constraintValidatorContext;
        this.escapeExpressions = escapeExpressions;
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     * <p>
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
                .buildConstraintViolationWithTemplate(sanitizeTemplate(message))
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     * <p>
     * Prefer the method with explicit message parameters if you want to interpolate the message.
     *
     * @param propertyName the name of the property
     * @param message      the message of the violation
     * @see #addViolation(String, String, Map)
     * @since 1.3.19
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
     * @since 1.3.21
     */
    public void addViolation(String propertyName, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        getContextWithMessageParameters(messageParameters)
                .buildConstraintViolationWithTemplate(sanitizeTemplate(message))
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
     * @since 1.3.19
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
     * @since 1.3.21
     */
    public void addViolation(String propertyName, Integer index, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        getContextWithMessageParameters(messageParameters)
                .buildConstraintViolationWithTemplate(sanitizeTemplate(message))
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
     * @since 1.3.19
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
     * @since 1.3.21
     */
    public void addViolation(String propertyName, String key, String message, Map<String, Object> messageParameters) {
        violationOccurred = true;
        final String messageTemplate = sanitizeTemplate(message);
        final HibernateConstraintValidatorContext context = getContextWithMessageParameters(messageParameters);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode(propertyName)
                .addBeanNode().inIterable().atKey(key)
                .addConstraintViolation();
    }

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

    @Nullable
    private String sanitizeTemplate(@Nullable String message) {
        return escapeExpressions ? escapeMessageParameter(message) : message;
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
