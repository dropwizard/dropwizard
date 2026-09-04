package io.dropwizard.servlets.assets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ByteRangeTest {

    private static final long RESOURCE_LENGTH = 10000L;

    @Test
    void firstBytes() {
        final ByteRange actual = ByteRange.parse("0-499", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isZero();
        assertThat(actual.getEnd()).isEqualTo(499L);
    }

    @Test
    void secondBytes() {
        final ByteRange actual = ByteRange.parse("500-999", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(500L);
        assertThat(actual.getEnd()).isEqualTo(999L);
    }

    @Test
    void finalBytes() {
        final ByteRange actual = ByteRange.parse("-500", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500L);
        assertThat(actual.getEnd()).isEqualTo(9999L);
    }

    @Test
    void noEndBytes() {
        final ByteRange actual = ByteRange.parse("9500-", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500L);
        assertThat(actual.getEnd()).isEqualTo(9999L);
    }

    @Test
    void startBytes() {
        final ByteRange actual = ByteRange.parse("9500", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500L);
        assertThat(actual.getEnd()).isEqualTo(9999L);
    }

    @Test
    void tooManyBytes() {
        final ByteRange actual = ByteRange.parse("9000-20000", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9000L);
        assertThat(actual.getEnd()).isEqualTo(9999L);
    }

    @Test
    void nonASCIIDisallowed() {
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> ByteRange.parse("០-០", RESOURCE_LENGTH));
    }

    @Test
    void malformedRangesThrowIllegalArgumentException() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ByteRange.parse(" -", RESOURCE_LENGTH));

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ByteRange.parse(" - ", RESOURCE_LENGTH));

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> ByteRange.parse("1-2-3", RESOURCE_LENGTH));
    }
}
