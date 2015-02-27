package io.dropwizard.configuration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assume.assumeThat;

public class EnvironmentVariableLookupTest {
    @Test(expected = UndefinedEnvironmentVariableException.class)
    public void defaultConstructorEnablesStrict() {
        assumeThat(System.getenv("nope"), nullValue());

        EnvironmentVariableLookup lookup = new EnvironmentVariableLookup();
        lookup.lookup("nope");
    }

    @Test
    public void lookupReplacesWithEnvironmentVariables() {
        EnvironmentVariableLookup lookup = new EnvironmentVariableLookup(false);

        // Let's hope this doesn't break on Windows
        assertThat(lookup.lookup("TEST")).isEqualTo(System.getenv("TEST"));
        assertThat(lookup.lookup("nope")).isNull();
    }

    @Test(expected = UndefinedEnvironmentVariableException.class)
    public void lookupThrowsExceptionInStrictMode() {
        assumeThat(System.getenv("nope"), nullValue());

        EnvironmentVariableLookup lookup = new EnvironmentVariableLookup(true);
        lookup.lookup("nope");
    }
}