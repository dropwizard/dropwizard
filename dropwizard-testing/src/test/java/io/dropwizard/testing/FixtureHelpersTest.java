package io.dropwizard.testing;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class FixtureHelpersTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();

    @Test
    public void readsTheFileAsAString() {
        assertThat(fixture("fixtures/fixture.txt")).isEqualTo("YAY FOR ME");
    }

    @Test
    public void throwsIllegalStateExceptionWhenFileDoesNotExist() {
        thrown.expect(IllegalStateException.class);
        fixture("this-does-not-exist.foo");
    }
}
