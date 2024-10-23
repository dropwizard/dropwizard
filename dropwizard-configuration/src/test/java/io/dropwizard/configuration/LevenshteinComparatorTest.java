package io.dropwizard.configuration;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

class LevenshteinComparatorTest {
    private final ConfigurationParsingException.Builder.LevenshteinComparator c = new ConfigurationParsingException.Builder.LevenshteinComparator("base");

    /**
     * A "java.lang.IllegalArgumentException: Comparison method violates its general contract!"
     * is triggered by this test with a previous version of LevenshteinComparator
     * <p>
     * It is triggered by a certain condition in TimSort that only happens if 32 or more
     * values are in an array. As such, it may not be a thorough test... it depends on the
     * specifics of the environment / JVM.
     */
    @Test
    void testLevenshteinComparatorSort() {
        assertThatNoException()
            .isThrownBy(() -> Arrays.sort(new String[]{
                "y", "w", "y", "e",
                "s", "u", "h", "o",
                "d", "t", "d", "f",
                "z", "j", "c", "k",
                "f", "z", "o", "e",
                "r", "t", "v", "d",
                "l", "r", "w", "u",
                "v", "a", "m", "o"}, c));
    }

    @Test
    void testLevenshteinCompare() {
        assertThat(c.compare("z", "v")).isZero();
        assertThat(c.compare("b", "v")).isEqualTo(-1);
        assertThat(c.compare("v", "b")).isEqualTo(1);
    }

}
