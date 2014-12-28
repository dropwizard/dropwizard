package io.dropwizard.testing;

import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class FixtureHelpersTest {
    @Test
    public void readsTheFileAsAString() {
        assertThat(fixture("fixtures/fixture.txt")).isEqualTo("YAY FOR ME");
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsIllegalStateExceptionWhenFileDoesNotExist() {
        fixture("this-does-not-exist.foo");
    }
}
