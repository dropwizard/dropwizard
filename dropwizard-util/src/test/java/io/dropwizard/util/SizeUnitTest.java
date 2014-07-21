package io.dropwizard.util;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SizeUnitTest {
    // BYTES

    @Test
    public void oneByteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.BYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.BYTES.toBytes(1))
                .isEqualTo(1);
    }


    @Test
    public void oneByteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toKilobytes(1))
                .isZero();
    }

    @Test
    public void oneByteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toMegabytes(1))
                .isZero();
    }

    @Test
    public void oneByteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    public void oneByteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toTerabytes(1))
                .isZero();
    }

    // KILOBYTES

    @Test
    public void oneKilobyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.KILOBYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.KILOBYTES.toBytes(1))
                .isEqualTo(1024);
    }

    @Test
    public void oneKilobyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.KILOBYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.KILOBYTES.toKilobytes(1))
                .isEqualTo(1L);
    }

    @Test
    public void oneKilobyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toMegabytes(1))
                .isZero();
    }

    @Test
    public void oneKilobyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    public void oneKilobyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toTerabytes(1))
                .isZero();
    }

    // MEGABYTES

    @Test
    public void oneMegabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.MEGABYTES.toBytes(1))
                .isEqualTo(1048576L);
    }

    @Test
    public void oneMegabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.MEGABYTES.toKilobytes(1))
                .isEqualTo(1024);
    }

    @Test
    public void oneMegabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.MEGABYTES.toMegabytes(1))
                .isEqualTo(1);
    }

    @Test
    public void oneMegabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.MEGABYTES))
                .isZero();

        assertThat(SizeUnit.MEGABYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    public void oneMegabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.MEGABYTES))
                .isZero();

        assertThat(SizeUnit.MEGABYTES.toTerabytes(1))
                .isZero();
    }

    // GIGABYTES

    @Test
    public void oneGigabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1073741824);

        assertThat(SizeUnit.GIGABYTES.toBytes(1))
                .isEqualTo(1073741824);
    }

    @Test
    public void oneGigabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.GIGABYTES.toKilobytes(1))
                .isEqualTo(1048576);
    }

    @Test
    public void oneGigabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.GIGABYTES.toMegabytes(1))
                .isEqualTo(1024);
    }

    @Test
    public void oneGigabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1L);

        assertThat(SizeUnit.GIGABYTES.toGigabytes(1))
                .isEqualTo(1L);
    }

    @Test
    public void oneGigabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.GIGABYTES))
                .isZero();

        assertThat(SizeUnit.GIGABYTES.toTerabytes(1))
                .isZero();
    }

    // TERABYTES

    @Test
    public void oneTerabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1099511627776L);

        assertThat(SizeUnit.TERABYTES.toBytes(1))
                .isEqualTo(1099511627776L);
    }

    @Test
    public void oneTerabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1073741824L);

        assertThat(SizeUnit.TERABYTES.toKilobytes(1))
                .isEqualTo(1073741824L);
    }

    @Test
    public void oneTerabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.TERABYTES.toMegabytes(1))
                .isEqualTo(1048576L);
    }

    @Test
    public void oneTerabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.TERABYTES.toGigabytes(1))
                .isEqualTo(1024);
    }

    @Test
    public void oneTerabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.TERABYTES.toTerabytes(1))
                .isEqualTo(1);
    }
}
