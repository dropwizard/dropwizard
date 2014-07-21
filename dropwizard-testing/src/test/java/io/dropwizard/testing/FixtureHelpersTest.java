package io.dropwizard.testing;

import org.junit.Test;

import static io.dropwizard.testing.FixtureHelpers.fixture;
import static org.assertj.core.api.Assertions.assertThat;

public class FixtureHelpersTest {
    @Test
    public void readsTheFileAsAString() throws Exception {
        assertThat(fixture("fixtures/fixture.txt"))
                .isEqualTo("YAY FOR ME");
    }
}
