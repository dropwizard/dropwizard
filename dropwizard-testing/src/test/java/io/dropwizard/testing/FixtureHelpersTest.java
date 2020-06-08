package io.dropwizard.testing;

import org.junit.jupiter.api.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class FixtureHelpersTest {
    @Test
    void readsTheFileAsAString() {
        assertThat(fixture("fixtures/fixture.txt")).isEqualTo("YAY FOR ME");
    }

    @Test
    void throwsIllegalStateExceptionWhenFileDoesNotExist() {
        assertThatIllegalArgumentException().isThrownBy(() -> fixture("this-does-not-exist.foo"));
    }
}
