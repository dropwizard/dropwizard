package io.dropwizard.servlets.assets;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ByteRangeTest {

    private static final int RESOURCE_LENGTH = 10000;

    @Test
    void firstBytes() {
        final ByteRange actual = ByteRange.parse("0-499", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isZero();
        assertThat(actual.getEnd()).isEqualTo(499);
    }

    @Test
    void secondBytes() {
        final ByteRange actual = ByteRange.parse("500-999", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(500);
        assertThat(actual.getEnd()).isEqualTo(999);
    }

    @Test
    void finalBytes() {
        final ByteRange actual = ByteRange.parse("-500", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500);
        assertThat(actual.getEnd()).isEqualTo(9999);
    }

    @Test
    void noEndBytes() {
        final ByteRange actual = ByteRange.parse("9500-", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500);
        assertThat(actual.getEnd()).isEqualTo(9999);
    }

    @Test
    void startBytes() {
        final ByteRange actual = ByteRange.parse("9500", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9500);
        assertThat(actual.getEnd()).isEqualTo(9999);
    }

    @Test
    void tooManyBytes() {
        final ByteRange actual = ByteRange.parse("9000-20000", RESOURCE_LENGTH);
        assertThat(actual.getStart()).isEqualTo(9000);
        assertThat(actual.getEnd()).isEqualTo(9999);
    }

    @Test
    void nonASCIIDisallowed() {
        assertThatExceptionOfType(NumberFormatException.class)
            .isThrownBy(() -> ByteRange.parse("០-០", RESOURCE_LENGTH));
    }
}
