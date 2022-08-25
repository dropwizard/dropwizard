package io.dropwizard.configuration;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.TextStringBuilder;

/**
 * A custom {@link StringSubstitutor} using environment variables as lookup source.
 */
public class EnvironmentVariableSubstitutor extends StringSubstitutor {
    /**
     * Constructs a new environment variable substitutor with strict checking and no substitution done in variables.
     */
    public EnvironmentVariableSubstitutor() {
        this(true, false);
    }

    /**
     * Constructs a new environment variable substitutor with no substitution done in variables.
     *
     * @param strict whether to use strict variable checking
     */
    public EnvironmentVariableSubstitutor(boolean strict) {
        this(strict, false);
    }

    /**
     * Constructs a new environment variable substitutor.
     *
     * @param strict                  {@code true} if looking up undefined environment variables should throw a
     *                                {@link UndefinedEnvironmentVariableException}, {@code false} otherwise.
     * @param substitutionInVariables a flag whether substitution is done in variable names.
     * @see org.apache.commons.text.StringSubstitutor#setEnableSubstitutionInVariables(boolean)
     */
    public EnvironmentVariableSubstitutor(boolean strict, boolean substitutionInVariables) {
        super(System::getenv);
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
