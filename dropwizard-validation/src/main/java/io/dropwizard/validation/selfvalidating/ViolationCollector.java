package io.dropwizard.validation.selfvalidating;

import javax.annotation.Nullable;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is a simple wrapper around the ConstraintValidatorContext of hibernate validation.
 * It collects all the violations of the SelfValidation methods of an object.
 */
public class ViolationCollector {
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\\$\\{");

    private boolean violationOccurred = false;
    private ConstraintValidatorContext context;


    public ViolationCollector(ConstraintValidatorContext context) {
        this.context = context;
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param message the message of the violation (any EL expression will be escaped and not parsed)
     */
    public void addViolation(String message) {
        violationOccurred = true;
        String messageTemplate = escapeEl(message);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName the name of the property
     * @param message      the message of the violation (any EL expression will be escaped and not parsed)
     * @since 2.0.2
     */
    public void addViolation(String propertyName, String message) {
        violationOccurred = true;
        String messageTemplate = escapeEl(message);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode(propertyName)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName the name of the property with the violation
     * @param index        the index of the element with the violation
     * @param message      the message of the violation (any EL expression will be escaped and not parsed)
     * @since 2.0.2
     */
    public void addViolation(String propertyName, Integer index, String message) {
        violationOccurred = true;
        String messageTemplate = escapeEl(message);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode(propertyName)
                .addBeanNode().inIterable().atIndex(index)
                .addConstraintViolation();
    }

    /**
     * Adds a new violation to this collector. This also sets {@code violationOccurred} to {@code true}.
     *
     * @param propertyName the name of the property with the violation
     * @param key          the key of the element with the violation
     * @param message      the message of the violation (any EL expression will be escaped and not parsed)
     * @since 2.0.2
     */
    public void addViolation(String propertyName, String key, String message) {
        violationOccurred = true;
        String messageTemplate = escapeEl(message);
        context.buildConstraintViolationWithTemplate(messageTemplate)
                .addPropertyNode(propertyName)
                .addBeanNode().inIterable().atKey(key)
                .addConstraintViolation();
    }

    @Nullable
    private String escapeEl(@Nullable String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }

        final Matcher m = ESCAPE_PATTERN.matcher(s);
        final StringBuffer sb = new StringBuffer(s.length() + 16);
        while (m.find()) {
            m.appendReplacement(sb, "\\\\\\${");
        }
        m.appendTail(sb);

        return sb.toString();
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
