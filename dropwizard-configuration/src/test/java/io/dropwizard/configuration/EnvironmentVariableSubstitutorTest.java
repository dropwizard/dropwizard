package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;

import org.junit.jupiter.api.Test;

public class EnvironmentVariableSubstitutorTest {

    @Test
    public void defaultConstructorDisablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        assertThat(substitutor.isEnableSubstitutionInVariables()).isFalse();
    }

    @Test
    public void defaultConstructorEnablesStrict() {
        assumeThat(System.getenv("DOES_NOT_EXIST")).isNull();

        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor().replace("${DOES_NOT_EXIST}"));
    }

    @Test
    public void constructorEnablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true, true);
        assertThat(substitutor.isEnableSubstitutionInVariables()).isTrue();
    }

    @Test
    public void substitutorUsesEnvironmentVariableLookup() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        assertThat(substitutor.getStringLookup()).isInstanceOf(EnvironmentVariableLookup.class);
    }

    @Test
    public void substitutorReplacesWithEnvironmentVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);

        assertThat(substitutor.replace("${TEST}")).isEqualTo(System.getenv("TEST"));
        assertThat(substitutor.replace("no replacement")).isEqualTo("no replacement");
        assertThat(substitutor.replace("${DOES_NOT_EXIST}")).isEqualTo("${DOES_NOT_EXIST}");
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-default}")).isEqualTo("default");
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-${TEST}}")).isEqualTo("${TEST}");
    }

    @Test
    public void substitutorStrictWithDefaults() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true);
        assertThat(substitutor.replace("${TEST} ${DOES_NOT_EXIST:-default}")).isEqualTo("test_value default");

        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor().replace("${TEST} ${DOES_NOT_EXIST}"));
    }

    @Test
    public void substitutorStrictRecurse() {
        assumeThat(System.getenv("DOES_NOT_EXIST")).isNull();
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true, true);
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-${TEST}}")).isEqualTo(System.getenv("TEST"));
    }

    @Test
    public void substitutorThrowsExceptionInStrictMode() {
        assumeThat(System.getenv("DOES_NOT_EXIST")).isNull();

        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor(true).replace("${DOES_NOT_EXIST}"));
    }

    @Test
    public void substitutorReplacesRecursively() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false, true);

        assertThat(substitutor.replace("$${${TEST}}")).isEqualTo("${test_value}");
        assertThat(substitutor.replace("$${$${${TEST}}}")).isEqualTo("${${test_value}}");
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX}}")).isEqualTo(System.getenv("TEST2"));
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX}3:-abc}")).isEqualTo("abc");
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX133:-2}:-abc}")).isEqualTo(System.getenv("TEST2"));
    }
}
