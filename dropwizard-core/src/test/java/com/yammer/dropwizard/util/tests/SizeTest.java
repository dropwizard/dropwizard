package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.util.SizeUnit;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SizeTest {
    @Test
    public void convertsToTerabytes() throws Exception {
        assertThat(Size.terabytes(2).toTerabytes())
                .isEqualTo(2);
    }

    @Test
    public void convertsToGigabytes() throws Exception {
        assertThat(Size.terabytes(2).toGigabytes())
                .isEqualTo(2048);
    }

    @Test
    public void convertsToMegabytes() throws Exception {
        assertThat(Size.gigabytes(2).toMegabytes())
                .isEqualTo(2048);
    }

    @Test
    public void convertsToKilobytes() throws Exception {
        assertThat(Size.megabytes(2).toKilobytes())
                .isEqualTo(2048);
    }

    @Test
    public void convertsToBytes() throws Exception {
        assertThat(Size.kilobytes(2).toBytes())
                .isEqualTo(2048L);
    }

    @Test
    public void parsesTerabytes() throws Exception {
        assertThat(Size.parse("2TB"))
                .isEqualTo(Size.terabytes(2));

        assertThat(Size.parse("2TiB"))
                .isEqualTo(Size.terabytes(2));

        assertThat(Size.parse("1 terabyte"))
                .isEqualTo(Size.terabytes(1));

        assertThat(Size.parse("2 terabytes"))
                .isEqualTo(Size.terabytes(2));
    }

    @Test
    public void parsesGigabytes() throws Exception {
        assertThat(Size.parse("2GB"))
                .isEqualTo(Size.gigabytes(2));

        assertThat(Size.parse("2GiB"))
                .isEqualTo(Size.gigabytes(2));

        assertThat(Size.parse("1 gigabyte"))
                .isEqualTo(Size.gigabytes(1));

        assertThat(Size.parse("2 gigabytes"))
                .isEqualTo(Size.gigabytes(2));
    }

    @Test
    public void parsesMegabytes() throws Exception {
        assertThat(Size.parse("2MB"))
                .isEqualTo(Size.megabytes(2));

        assertThat(Size.parse("2MiB"))
                .isEqualTo(Size.megabytes(2));

        assertThat(Size.parse("1 megabyte"))
                .isEqualTo(Size.megabytes(1));

        assertThat(Size.parse("2 megabytes"))
                .isEqualTo(Size.megabytes(2));
    }

    @Test
    public void parsesKilobytes() throws Exception {
        assertThat(Size.parse("2KB"))
                .isEqualTo(Size.kilobytes(2));

        assertThat(Size.parse("2KiB"))
                .isEqualTo(Size.kilobytes(2));

        assertThat(Size.parse("1 kilobyte"))
                .isEqualTo(Size.kilobytes(1));

        assertThat(Size.parse("2 kilobytes"))
                .isEqualTo(Size.kilobytes(2));
    }

    @Test
    public void parsesBytes() throws Exception {
        assertThat(Size.parse("2B"))
                .isEqualTo(Size.bytes(2));

        assertThat(Size.parse("1 byte"))
                .isEqualTo(Size.bytes(1));

        assertThat(Size.parse("2 bytes"))
                .isEqualTo(Size.bytes(2));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(Size.gigabytes(3).toString())
                .isEqualTo("3 gigabytes");

        assertThat(Size.kilobytes(1).toString())
                .isEqualTo("1 kilobyte");
    }

    @Test
    public void hasAQuantity() throws Exception {
        assertThat(Size.gigabytes(3).getQuantity())
                .isEqualTo(3);
    }

    @Test
    public void hasAUnit() throws Exception {
        assertThat(Size.gigabytes(3).getUnit())
                .isEqualTo(SizeUnit.GIGABYTES);
    }
}
