package io.dropwizard.configuration;

import org.apache.commons.text.StringSubstitutor;

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
     * @see io.dropwizard.configuration.EnvironmentVariableLookup#EnvironmentVariableLookup(boolean)
     * @see org.apache.commons.text.StringSubstitutor#setEnableSubstitutionInVariables(boolean)
     */
    public EnvironmentVariableSubstitutor(boolean strict, boolean substitutionInVariables) {
        super(new EnvironmentVariableLookup(strict));
        this.setEnableSubstitutionInVariables(substitutionInVariables);
    }
}
