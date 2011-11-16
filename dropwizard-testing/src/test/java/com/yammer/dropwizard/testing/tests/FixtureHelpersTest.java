package com.yammer.dropwizard.testing.tests;

import org.junit.Test;

import static com.yammer.dropwizard.testing.FixtureHelpers.fixture;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class FixtureHelpersTest {
    @Test
    public void readsTheFileAsAString() throws Exception {
        assertThat(fixture("fixtures/fixture.txt"),
                   is("YAY FOR ME"));
    }
}
