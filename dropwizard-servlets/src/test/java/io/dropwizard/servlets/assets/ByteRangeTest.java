package io.dropwizard.servlets.assets;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

public class ByteRangeTest {

    @Test
    public void firstBytes() {
        final ByteRange actual = ByteRange.parse("0-499");
        assertThat(actual.getStart()).isEqualTo(0);
        assertThat(actual.getEnd()).isEqualTo(499);
        assertThat(actual.hasEnd()).isTrue();
    }

    @Test
    public void secondBytes() {
        final ByteRange actual = ByteRange.parse("500-999");
        assertThat(actual.getStart()).isEqualTo(500);
        assertThat(actual.getEnd()).isEqualTo(999);
        assertThat(actual.hasEnd()).isTrue();
    }

    @Test
    public void finalBytes() {
        final ByteRange actual = ByteRange.parse("-500");
        assertThat(actual.getStart()).isEqualTo(-500);
        assertThat(actual.hasEnd()).isFalse();
    }
    
    @Test
    public void noEndBytes() {
        final ByteRange actual = ByteRange.parse("9500-");
        assertThat(actual.getStart()).isEqualTo(9500);
        assertThat(actual.hasEnd()).isFalse();
    }
}
