package io.dropwizard.configuration;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;

/**
 * A custom {@link StringSubstitutor} using environment variables as lookup source.
 */
public class EnvironmentVariableSubstitutor extends StringSubstitutor {
    public EnvironmentVariableSubstitutor() {
        this(true, false);
    }

    public EnvironmentVariableSubstitutor(boolean strict) {
        this(strict, false);
    }

    /**
     * @param strict                  {@code true} if looking up undefined environment variables should throw a
     *                                {@link UndefinedEnvironmentVariableException}, {@code false} otherwise.
     * @param substitutionInVariables a flag whether substitution is done in variable names.
     * @see org.apache.commons.text.StringSubstitutor#setEnableSubstitutionInVariables(boolean)
     */
    public EnvironmentVariableSubstitutor(boolean strict, boolean substitutionInVariables) {
        super(new EnvironmentVariableLookup());
        this.setEnableUndefinedVariableException(strict);
        this.setEnableSubstitutionInVariables(substitutionInVariables);
    }

    @Override
    protected boolean substitute(TextStringBuilder buf, int offset, int length) {
        try {
            return super.substitute(buf, offset, length);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null && e.getMessage().contains("Cannot resolve variable")) {
                throw new UndefinedEnvironmentVariableException(e.getMessage());
            }
            throw e;
        }
    }
}
