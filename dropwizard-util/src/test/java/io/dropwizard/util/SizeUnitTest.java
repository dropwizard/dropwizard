package io.dropwizard.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class SizeUnitTest {
    // BYTES
    @Test
    void oneByteInBytes() {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.BYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.BYTES.toBytes(1))
                .isEqualTo(1);
    }

    @Test
    void oneByteInKilobytes() {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toKilobytes(1))
                .isZero();
    }

    @Test
    void oneByteInMegabytes() {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toMegabytes(1))
                .isZero();
    }

    @Test
    void oneByteInGigabytes() {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    void oneByteInTerabytes() {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.BYTES))
                .isZero();

        assertThat(SizeUnit.BYTES.toTerabytes(1))
                .isZero();
    }

    // KILOBYTES

    @Test
    void oneKilobyteInBytes() {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.KILOBYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.KILOBYTES.toBytes(1))
                .isEqualTo(1024);
    }

    @Test
    void oneKilobyteInKilobytes() {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.KILOBYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.KILOBYTES.toKilobytes(1))
                .isEqualTo(1L);
    }

    @Test
    void oneKilobyteInMegabytes() {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toMegabytes(1))
                .isZero();
    }

    @Test
    void oneKilobyteInGigabytes() {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    void oneKilobyteInTerabytes() {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.KILOBYTES))
                .isZero();

        assertThat(SizeUnit.KILOBYTES.toTerabytes(1))
                .isZero();
    }

    // MEGABYTES

    @Test
    void oneMegabyteInBytes() {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.MEGABYTES.toBytes(1))
                .isEqualTo(1048576L);
    }

    @Test
    void oneMegabyteInKilobytes() {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.MEGABYTES.toKilobytes(1))
                .isEqualTo(1024);
    }

    @Test
    void oneMegabyteInMegabytes() {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.MEGABYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.MEGABYTES.toMegabytes(1))
                .isEqualTo(1);
    }

    @Test
    void oneMegabyteInGigabytes() {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.MEGABYTES))
                .isZero();

        assertThat(SizeUnit.MEGABYTES.toGigabytes(1))
                .isZero();
    }

    @Test
    void oneMegabyteInTerabytes() {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.MEGABYTES))
                .isZero();

        assertThat(SizeUnit.MEGABYTES.toTerabytes(1))
                .isZero();
    }

    // GIGABYTES

    @Test
    void oneGigabyteInBytes() {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1073741824);

        assertThat(SizeUnit.GIGABYTES.toBytes(1))
                .isEqualTo(1073741824);
    }

    @Test
    void oneGigabyteInKilobytes() {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.GIGABYTES.toKilobytes(1))
                .isEqualTo(1048576);
    }

    @Test
    void oneGigabyteInMegabytes() {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.GIGABYTES.toMegabytes(1))
                .isEqualTo(1024);
    }

    @Test
    void oneGigabyteInGigabytes() {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.GIGABYTES))
                .isEqualTo(1L);

        assertThat(SizeUnit.GIGABYTES.toGigabytes(1))
                .isEqualTo(1L);
    }

    @Test
    void oneGigabyteInTerabytes() {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.GIGABYTES))
                .isZero();

        assertThat(SizeUnit.GIGABYTES.toTerabytes(1))
                .isZero();
    }

    // TERABYTES

    @Test
    void oneTerabyteInBytes() {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1099511627776L);

        assertThat(SizeUnit.TERABYTES.toBytes(1))
                .isEqualTo(1099511627776L);
    }

    @Test
    void oneTerabyteInKilobytes() {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1073741824L);

        assertThat(SizeUnit.TERABYTES.toKilobytes(1))
                .isEqualTo(1073741824L);
    }

    @Test
    void oneTerabyteInMegabytes() {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1048576);

        assertThat(SizeUnit.TERABYTES.toMegabytes(1))
                .isEqualTo(1048576L);
    }

    @Test
    void oneTerabyteInGigabytes() {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1024);

        assertThat(SizeUnit.TERABYTES.toGigabytes(1))
                .isEqualTo(1024);
    }

    @Test
    void oneTerabyteInTerabytes() {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.TERABYTES))
                .isEqualTo(1);

        assertThat(SizeUnit.TERABYTES.toTerabytes(1))
                .isEqualTo(1);
    }

    @Test
    void testToDataSizeUnit() {
        assertThat(SizeUnit.BYTES.toDataSizeUnit()).isEqualTo(DataSizeUnit.BYTES);
        assertThat(SizeUnit.KILOBYTES.toDataSizeUnit()).isEqualTo(DataSizeUnit.KIBIBYTES);
        assertThat(SizeUnit.MEGABYTES.toDataSizeUnit()).isEqualTo(DataSizeUnit.MEBIBYTES);
        assertThat(SizeUnit.GIGABYTES.toDataSizeUnit()).isEqualTo(DataSizeUnit.GIBIBYTES);
        assertThat(SizeUnit.TERABYTES.toDataSizeUnit()).isEqualTo(DataSizeUnit.TEBIBYTES);
    }
}
