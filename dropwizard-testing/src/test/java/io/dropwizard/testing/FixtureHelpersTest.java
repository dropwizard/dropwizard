package io.dropwizard.testing;

import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class FixtureHelpersTest {
    @Test
    public void readsTheFileAsAString() {
        assertThat(fixture("fixtures/fixture.txt")).isEqualTo("YAY FOR ME");
    }

    @Test
    public void throwsIllegalStateExceptionWhenFileDoesNotExist() {
        assertThatIllegalArgumentException().isThrownBy(() -> fixture("this-does-not-exist.foo"));
    }
}
