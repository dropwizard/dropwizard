package io.dropwizard.configuration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assumptions.assumeThat;

class EnvironmentVariableLookupTest {
    @Test
    void lookupThrowsExceptionInStrictMode() {
        assumeThat(System.getenv("nope")).isNull();
        assertThat(new EnvironmentVariableLookup().lookup("nope")).isNull();
    }

    @Test
    void lookupReplacesWithEnvironmentVariables() {
        EnvironmentVariableLookup lookup = new EnvironmentVariableLookup();

        // Let's hope this doesn't break on Windows
        assertThat(lookup.lookup("TEST")).isEqualTo(System.getenv("TEST"));
        assertThat(lookup.lookup("nope")).isNull();
    }
}
