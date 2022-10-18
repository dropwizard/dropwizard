package io.dropwizard.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

class EnvironmentVariableSubstitutorTest {

    @Test
    void defaultConstructorDisablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor();
        assertThat(substitutor.isEnableSubstitutionInVariables()).isFalse();
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "DOES_NOT_EXIST", matches = ".*")
    void defaultConstructorEnablesStrict() {
        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor().replace("${DOES_NOT_EXIST}"));
    }

    @Test
    void constructorEnablesSubstitutionInVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true, true);
        assertThat(substitutor.isEnableSubstitutionInVariables()).isTrue();
    }

    @Test
    void substitutorReplacesWithEnvironmentVariables() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false);

        assertThat(substitutor.replace("${TEST}")).isEqualTo(System.getenv("TEST"));
        assertThat(substitutor.replace("no replacement")).isEqualTo("no replacement");
        assertThat(substitutor.replace("${DOES_NOT_EXIST}")).isEqualTo("${DOES_NOT_EXIST}");
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-default}")).isEqualTo("default");
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-${TEST}}")).isEqualTo("${TEST}");
    }

    @Test
    void substitutorStrictWithDefaults() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true);
        assertThat(substitutor.replace("${TEST} ${DOES_NOT_EXIST:-default}")).isEqualTo("test_value default");

        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor().replace("${TEST} ${DOES_NOT_EXIST}"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "DOES_NOT_EXIST", matches = ".*")
    void substitutorStrictRecurse() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(true, true);
        assertThat(substitutor.replace("${DOES_NOT_EXIST:-${TEST}}")).isEqualTo(System.getenv("TEST"));
    }

    @Test
    @DisabledIfEnvironmentVariable(named = "DOES_NOT_EXIST", matches = ".*")
    void substitutorThrowsExceptionInStrictMode() {
        assertThatExceptionOfType(UndefinedEnvironmentVariableException.class).isThrownBy(() ->
            new EnvironmentVariableSubstitutor(true).replace("${DOES_NOT_EXIST}"));
    }

    @Test
    void substitutorReplacesRecursively() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false, true);

        assertThat(substitutor.replace("$${${TEST}}")).isEqualTo("${test_value}");
        assertThat(substitutor.replace("$${$${${TEST}}}")).isEqualTo("${${test_value}}");
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX}}")).isEqualTo(System.getenv("TEST2"));
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX}3:-abc}")).isEqualTo("abc");
        assertThat(substitutor.replace("${TEST${TEST_SUFFIX133:-2}:-abc}")).isEqualTo(System.getenv("TEST2"));
    }
    
    @Test
    void shouldNotBeVulnerableToCVE_2022_42889() {
        EnvironmentVariableSubstitutor substitutor = new EnvironmentVariableSubstitutor(false, false);
        assertThat(substitutor.replace("${script:javascript:3 + 4}")).isEqualTo(System.getenv("${script:javascript:3 + 4}"));
    }
}
