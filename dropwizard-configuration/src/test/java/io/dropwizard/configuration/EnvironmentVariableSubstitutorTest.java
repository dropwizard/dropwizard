package io.dropwizard.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assume.assumeThat;

public class EnvironmentVariableSubstitutorTest {
    @Test
    public void defaultConstructorDisablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        assertThat(substitutor.isEnableSubstitutionInVariables()).isFalse();
    }

    @Test(expected = UndefinedEnvironmentVariableException.class)
    public void defaultConstructorEnablesStrict() {
        assumeThat(System.getenv("DOES_NOT_EXIST"), nullValue());

        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        substitutor.replace("${DOES_NOT_EXIST}");
    }

    @Test
    public void constructorEnablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true, true);
        assertThat(substitutor.isEnableSubstitutionInVariables()).isTrue();
    }

    @Test
    public void substitutorUsesEnvironmentVariableLookup() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        assertThat(substitutor.getVariableResolver()).isInstanceOf(EnvironmentVariableLookup.class);
    }

    @Test
    public void substitutorReplacesWithEnvironmentVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);

        assertThat(substitutor.replace("${TEST}")).isEqualTo(System.getenv("TEST"));
        assertThat(substitutor.replace("no replacement")).isEqualTo("no replacement");
        assertThat(substitutor.replace("${DOES_NOT_EXIST}")).isEqualTo("${DOES_NOT_EXIST}");
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-default}")).isEqualTo("default");
    }

    @Test(expected = UndefinedEnvironmentVariableException.class)
    public void substitutorThrowsExceptionInStrictMode() {
        assumeThat(System.getenv("DOES_NOT_EXIST"), nullValue());

        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true);
        substitutor.replace("${DOES_NOT_EXIST}");
    }

    @Test
    public void substitutorReplacesRecursively() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false, true);

        assertThat(substitutor.replace("$${${TEST}}")).isEqualTo("${test_value}");
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX}}")).isEqualTo(System.getenv("TEST2"));
    }
}