package io.dropwizard.configuration;

import java.util.Collection;

/**
 * Base class for problems with a Configuration object.
 * <p/>
 * Refer to the implementations for different classes of problems:
 * <ul>
 *     <li>Parsing errors: {@link ConfigurationParsingException}</li>
 *     <li>Validation errors: {@link ConfigurationValidationException}</li>
 * </ul>
 */
public abstract class ConfigurationException extends Exception {
    protected static final String NEWLINE = String.format("%n");

    private final Collection<String> errors;

    /**
     * Creates a new ConfigurationException for the given path with the given errors.
     *
     * @param path      the bad configuration path
     * @param errors    the errors in the path
     */
    public ConfigurationException(String path, Collection<String> errors) {
        super(formatMessage(path, errors));
        this.errors = errors;
    }

    /**
     * Creates a new ConfigurationException for the given path with the given errors and cause.
     *
     * @param path      the bad configuration path
     * @param errors    the errors in the path
     * @param cause     the cause of the error(s)
     */
    public ConfigurationException(String path, Collection<String> errors, Throwable cause) {
        super(formatMessage(path, errors), cause);
        this.errors = errors;
    }

    public Collection<String> getErrors() {
        return errors;
    }

    protected static String formatMessage(String file, Collection<String> errors) {
        final StringBuilder msg = new StringBuilder(file);
        msg.append(errors.size() == 1 ? " has an error:" : " has the following errors:").append(NEWLINE);
        for (String error : errors) {
            msg.append("  * ").append(error).append(NEWLINE);
        }
        return msg.toString();
    }
}
