package io.dropwizard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DataSizeTest {
    @Test
    void convertsToPetabytes() {
        assertThat(DataSize.petabytes(2).toPetabytes())
                .isEqualTo(2);
    }

    @Test
    void convertsToTerabytes() {
        assertThat(DataSize.terabytes(2).toTerabytes())
                .isEqualTo(2);
    }

    @Test
    void convertsToGigabytes() {
        assertThat(DataSize.terabytes(2).toGigabytes())
                .isEqualTo(2000);
    }

    @Test
    void convertsToMegabytes() {
        assertThat(DataSize.gigabytes(2).toMegabytes())
                .isEqualTo(2000);
    }

    @Test
    void convertsToKilobytes() {
        assertThat(DataSize.megabytes(2).toKilobytes())
                .isEqualTo(2000);
    }

    @Test
    void convertsToBytes() {
        assertThat(DataSize.kilobytes(2).toBytes())
                .isEqualTo(2000L);
    }

    @Test
    void parsesPetabytes() {
        assertThat(DataSize.parse("2P"))
                .isEqualTo(DataSize.petabytes(2));

        assertThat(DataSize.parse("2PB"))
                .isEqualTo(DataSize.petabytes(2));

        assertThat(DataSize.parse("1 petabyte"))
                .isEqualTo(DataSize.petabytes(1));

        assertThat(DataSize.parse("2 petabytes"))
                .isEqualTo(DataSize.petabytes(2));
    }

    @Test
    void parsesPebibytes() {
        assertThat(DataSize.parse("2PiB"))
                .isEqualTo(DataSize.pebibytes(2));

        assertThat(DataSize.parse("1 pebibyte"))
                .isEqualTo(DataSize.pebibytes(1));

        assertThat(DataSize.parse("2 pebibytes"))
                .isEqualTo(DataSize.pebibytes(2));
    }

    @Test
    void parsesTerabytes() {
        assertThat(DataSize.parse("2T"))
                .isEqualTo(DataSize.terabytes(2));

        assertThat(DataSize.parse("2TB"))
                .isEqualTo(DataSize.terabytes(2));

        assertThat(DataSize.parse("1 terabyte"))
                .isEqualTo(DataSize.terabytes(1));

        assertThat(DataSize.parse("2 terabytes"))
                .isEqualTo(DataSize.terabytes(2));
    }

    @Test
    void parsesTebibytes() {
        assertThat(DataSize.parse("2TiB"))
                .isEqualTo(DataSize.tebibytes(2));

        assertThat(DataSize.parse("1 tebibyte"))
                .isEqualTo(DataSize.tebibytes(1));

        assertThat(DataSize.parse("2 tebibytes"))
                .isEqualTo(DataSize.tebibytes(2));
    }

    @Test
    void parsesGigabytes() {
        assertThat(DataSize.parse("2G"))
                .isEqualTo(DataSize.gigabytes(2));

        assertThat(DataSize.parse("2GB"))
                .isEqualTo(DataSize.gigabytes(2));

        assertThat(DataSize.parse("1 gigabyte"))
                .isEqualTo(DataSize.gigabytes(1));

        assertThat(DataSize.parse("2 gigabytes"))
                .isEqualTo(DataSize.gigabytes(2));
    }

    @Test
    void parsesGibibytes() {
        assertThat(DataSize.parse("2GiB"))
                .isEqualTo(DataSize.gibibytes(2));

        assertThat(DataSize.parse("1 gibibyte"))
                .isEqualTo(DataSize.gibibytes(1));

        assertThat(DataSize.parse("2 gibibytes"))
                .isEqualTo(DataSize.gibibytes(2));
    }

    @Test
    void parsesMegabytes() {
        assertThat(DataSize.parse("2M"))
                .isEqualTo(DataSize.megabytes(2));

        assertThat(DataSize.parse("2MB"))
                .isEqualTo(DataSize.megabytes(2));

        assertThat(DataSize.parse("1 megabyte"))
                .isEqualTo(DataSize.megabytes(1));

        assertThat(DataSize.parse("2 megabytes"))
                .isEqualTo(DataSize.megabytes(2));
    }

    @Test
    void parsesMebibytes() {
        assertThat(DataSize.parse("2MiB"))
                .isEqualTo(DataSize.mebibytes(2));

        assertThat(DataSize.parse("1 mebibyte"))
                .isEqualTo(DataSize.mebibytes(1));

        assertThat(DataSize.parse("2 mebibytes"))
                .isEqualTo(DataSize.mebibytes(2));
    }

    @Test
    void parsesKilobytes() {
        assertThat(DataSize.parse("2K"))
                .isEqualTo(DataSize.kilobytes(2));

        assertThat(DataSize.parse("2KB"))
                .isEqualTo(DataSize.kilobytes(2));

        assertThat(DataSize.parse("1 kilobyte"))
                .isEqualTo(DataSize.kilobytes(1));

        assertThat(DataSize.parse("2 kilobytes"))
                .isEqualTo(DataSize.kilobytes(2));
    }

    @Test
    void parsesKibibytes() {
        assertThat(DataSize.parse("2KiB"))
                .isEqualTo(DataSize.kibibytes(2));

        assertThat(DataSize.parse("1 kibibyte"))
                .isEqualTo(DataSize.kibibytes(1));

        assertThat(DataSize.parse("2 kibibytes"))
                .isEqualTo(DataSize.kibibytes(2));
    }

    @Test
    void parsesBytes() {
        assertThat(DataSize.parse("2B"))
                .isEqualTo(DataSize.bytes(2));

        assertThat(DataSize.parse("1 byte"))
                .isEqualTo(DataSize.bytes(1));

        assertThat(DataSize.parse("2 bytes"))
                .isEqualTo(DataSize.bytes(2));

        assertThat(DataSize.parse("2"))
                .isEqualTo(DataSize.bytes(2));
    }

    @Test
    void parseDataSizeWithWhiteSpaces() {
        assertThat(DataSize.parse("64   kilobytes"))
                .isEqualTo(DataSize.kilobytes(64));
    }

    @Test
    void parseCaseInsensitive() {
        assertThat(DataSize.parse("1b")).isEqualTo(DataSize.parse("1B"));
    }

    @Test
    void parseSingleLetterSuffix() {
        assertThat(DataSize.parse("1B")).isEqualTo(DataSize.bytes(1));
        assertThat(DataSize.parse("1K")).isEqualTo(DataSize.kilobytes(1));
        assertThat(DataSize.parse("1M")).isEqualTo(DataSize.megabytes(1));
        assertThat(DataSize.parse("1G")).isEqualTo(DataSize.gigabytes(1));
        assertThat(DataSize.parse("1T")).isEqualTo(DataSize.terabytes(1));
        assertThat(DataSize.parse("1P")).isEqualTo(DataSize.petabytes(1));
    }

    @Test
    void unableParseWrongDataSizeCount() {
        assertThatThrownBy(() -> DataSize.parse("three bytes"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid size: three bytes");
    }

    @Test
    void unableParseWrongDataSizeUnit() {
        assertThatThrownBy(() -> DataSize.parse("1EB"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid size: 1EB. Wrong size unit");
    }

    @Test
    void unableParseWrongDataSizeFormat() {
        assertThatThrownBy(() -> DataSize.parse("1 mega byte"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid size: 1 mega byte");
    }

    @Test
    void isHumanReadable() {
        assertThat(DataSize.gigabytes(3).toString())
                .isEqualTo("3 gigabytes");

        assertThat(DataSize.kilobytes(1).toString())
                .isEqualTo("1 kilobyte");
    }

    @Test
    void hasAQuantity() {
        assertThat(DataSize.gigabytes(3).getQuantity())
                .isEqualTo(3);
    }

    @Test
    void hasAUnit() {
        assertThat(DataSize.gigabytes(3).getUnit())
                .isEqualTo(DataSizeUnit.GIGABYTES);
    }

    @Test
    void isComparable() {
        // both zero
        assertThat(DataSize.bytes(0).compareTo(DataSize.bytes(0))).isEqualTo(0);
        assertThat(DataSize.bytes(0).compareTo(DataSize.kilobytes(0))).isEqualTo(0);
        assertThat(DataSize.bytes(0).compareTo(DataSize.megabytes(0))).isEqualTo(0);
        assertThat(DataSize.bytes(0).compareTo(DataSize.gigabytes(0))).isEqualTo(0);
        assertThat(DataSize.bytes(0).compareTo(DataSize.terabytes(0))).isEqualTo(0);

        assertThat(DataSize.kilobytes(0).compareTo(DataSize.bytes(0))).isEqualTo(0);
        assertThat(DataSize.kilobytes(0).compareTo(DataSize.kilobytes(0))).isEqualTo(0);
        assertThat(DataSize.kilobytes(0).compareTo(DataSize.megabytes(0))).isEqualTo(0);
        assertThat(DataSize.kilobytes(0).compareTo(DataSize.gigabytes(0))).isEqualTo(0);
        assertThat(DataSize.kilobytes(0).compareTo(DataSize.terabytes(0))).isEqualTo(0);

        assertThat(DataSize.megabytes(0).compareTo(DataSize.bytes(0))).isEqualTo(0);
        assertThat(DataSize.megabytes(0).compareTo(DataSize.kilobytes(0))).isEqualTo(0);
        assertThat(DataSize.megabytes(0).compareTo(DataSize.megabytes(0))).isEqualTo(0);
        assertThat(DataSize.megabytes(0).compareTo(DataSize.gigabytes(0))).isEqualTo(0);
        assertThat(DataSize.megabytes(0).compareTo(DataSize.terabytes(0))).isEqualTo(0);

        assertThat(DataSize.gigabytes(0).compareTo(DataSize.bytes(0))).isEqualTo(0);
        assertThat(DataSize.gigabytes(0).compareTo(DataSize.kilobytes(0))).isEqualTo(0);
        assertThat(DataSize.gigabytes(0).compareTo(DataSize.megabytes(0))).isEqualTo(0);
        assertThat(DataSize.gigabytes(0).compareTo(DataSize.gigabytes(0))).isEqualTo(0);
        assertThat(DataSize.gigabytes(0).compareTo(DataSize.terabytes(0))).isEqualTo(0);

        assertThat(DataSize.terabytes(0).compareTo(DataSize.bytes(0))).isEqualTo(0);
        assertThat(DataSize.terabytes(0).compareTo(DataSize.kilobytes(0))).isEqualTo(0);
        assertThat(DataSize.terabytes(0).compareTo(DataSize.megabytes(0))).isEqualTo(0);
        assertThat(DataSize.terabytes(0).compareTo(DataSize.gigabytes(0))).isEqualTo(0);
        assertThat(DataSize.terabytes(0).compareTo(DataSize.terabytes(0))).isEqualTo(0);

        // one zero, one negative
        assertThat(DataSize.bytes(0)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.bytes(0)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.bytes(0)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.bytes(0)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.bytes(0)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.kilobytes(0)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.kilobytes(0)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.kilobytes(0)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.kilobytes(0)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.kilobytes(0)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.megabytes(0)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.megabytes(0)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.megabytes(0)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.megabytes(0)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.megabytes(0)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.gigabytes(0)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.gigabytes(0)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.gigabytes(0)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.gigabytes(0)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.gigabytes(0)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.terabytes(0)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.terabytes(0)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.terabytes(0)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.terabytes(0)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.terabytes(0)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.bytes(0));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.kilobytes(0));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.megabytes(0));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.gigabytes(0));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.terabytes(0));

        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.bytes(0));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.kilobytes(0));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.megabytes(0));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.gigabytes(0));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.terabytes(0));

        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.bytes(0));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.kilobytes(0));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.megabytes(0));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.gigabytes(0));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.terabytes(0));

        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.bytes(0));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.kilobytes(0));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.megabytes(0));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.gigabytes(0));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.terabytes(0));

        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.bytes(0));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.kilobytes(0));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.megabytes(0));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.gigabytes(0));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.terabytes(0));

        // one zero, one positive
        assertThat(DataSize.bytes(0)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.bytes(0)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.bytes(0)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.bytes(0)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.bytes(0)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.kilobytes(0)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.kilobytes(0)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.kilobytes(0)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.kilobytes(0)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.kilobytes(0)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.megabytes(0)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.megabytes(0)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.megabytes(0)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.megabytes(0)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.megabytes(0)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.gigabytes(0)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.gigabytes(0)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.gigabytes(0)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.gigabytes(0)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.gigabytes(0)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.terabytes(0)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.terabytes(0)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.terabytes(0)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.terabytes(0)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.terabytes(0)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.bytes(0));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.kilobytes(0));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.megabytes(0));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.gigabytes(0));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.terabytes(0));

        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.bytes(0));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.kilobytes(0));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.megabytes(0));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.gigabytes(0));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.terabytes(0));

        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.bytes(0));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.kilobytes(0));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.megabytes(0));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.gigabytes(0));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.terabytes(0));

        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.bytes(0));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.kilobytes(0));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.megabytes(0));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.gigabytes(0));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.terabytes(0));

        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.bytes(0));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.kilobytes(0));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.megabytes(0));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.gigabytes(0));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.terabytes(0));

        // both negative
        assertThat(DataSize.bytes(-2)).isLessThan(DataSize.bytes(-1));
        assertThat(DataSize.bytes(-2)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.bytes(-2)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.bytes(-2)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.bytes(-2)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.kilobytes(-2)).isLessThan(DataSize.bytes(-1));
        assertThat(DataSize.kilobytes(-2)).isLessThan(DataSize.kilobytes(-1));
        assertThat(DataSize.kilobytes(-2)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.kilobytes(-2)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.kilobytes(-2)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.megabytes(-2)).isLessThan(DataSize.bytes(-1));
        assertThat(DataSize.megabytes(-2)).isLessThan(DataSize.kilobytes(-1));
        assertThat(DataSize.megabytes(-2)).isLessThan(DataSize.megabytes(-1));
        assertThat(DataSize.megabytes(-2)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.megabytes(-2)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.gigabytes(-2)).isLessThan(DataSize.bytes(-1));
        assertThat(DataSize.gigabytes(-2)).isLessThan(DataSize.kilobytes(-1));
        assertThat(DataSize.gigabytes(-2)).isLessThan(DataSize.megabytes(-1));
        assertThat(DataSize.gigabytes(-2)).isLessThan(DataSize.gigabytes(-1));
        assertThat(DataSize.gigabytes(-2)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.terabytes(-2)).isLessThan(DataSize.bytes(-1));
        assertThat(DataSize.terabytes(-2)).isLessThan(DataSize.kilobytes(-1));
        assertThat(DataSize.terabytes(-2)).isLessThan(DataSize.megabytes(-1));
        assertThat(DataSize.terabytes(-2)).isLessThan(DataSize.gigabytes(-1));
        assertThat(DataSize.terabytes(-2)).isLessThan(DataSize.terabytes(-1));

        assertThat(DataSize.bytes(-1)).isGreaterThan(DataSize.bytes(-2));
        assertThat(DataSize.bytes(-1)).isGreaterThan(DataSize.kilobytes(-2));
        assertThat(DataSize.bytes(-1)).isGreaterThan(DataSize.megabytes(-2));
        assertThat(DataSize.bytes(-1)).isGreaterThan(DataSize.gigabytes(-2));
        assertThat(DataSize.bytes(-1)).isGreaterThan(DataSize.terabytes(-2));

        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.bytes(-2));
        assertThat(DataSize.kilobytes(-1)).isGreaterThan(DataSize.kilobytes(-2));
        assertThat(DataSize.kilobytes(-1)).isGreaterThan(DataSize.megabytes(-2));
        assertThat(DataSize.kilobytes(-1)).isGreaterThan(DataSize.gigabytes(-2));
        assertThat(DataSize.kilobytes(-1)).isGreaterThan(DataSize.terabytes(-2));

        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.bytes(-2));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.kilobytes(-2));
        assertThat(DataSize.megabytes(-1)).isGreaterThan(DataSize.megabytes(-2));
        assertThat(DataSize.megabytes(-1)).isGreaterThan(DataSize.gigabytes(-2));
        assertThat(DataSize.megabytes(-1)).isGreaterThan(DataSize.terabytes(-2));

        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.bytes(-2));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.kilobytes(-2));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.megabytes(-2));
        assertThat(DataSize.gigabytes(-1)).isGreaterThan(DataSize.gigabytes(-2));
        assertThat(DataSize.gigabytes(-1)).isGreaterThan(DataSize.terabytes(-2));

        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.bytes(-2));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.kilobytes(-2));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.megabytes(-2));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.gigabytes(-2));
        assertThat(DataSize.terabytes(-1)).isGreaterThan(DataSize.terabytes(-2));

        // both positive
        assertThat(DataSize.bytes(1)).isLessThan(DataSize.bytes(2));
        assertThat(DataSize.bytes(1)).isLessThan(DataSize.kilobytes(2));
        assertThat(DataSize.bytes(1)).isLessThan(DataSize.megabytes(2));
        assertThat(DataSize.bytes(1)).isLessThan(DataSize.gigabytes(2));
        assertThat(DataSize.bytes(1)).isLessThan(DataSize.terabytes(2));

        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.bytes(2));
        assertThat(DataSize.kilobytes(1)).isLessThan(DataSize.kilobytes(2));
        assertThat(DataSize.kilobytes(1)).isLessThan(DataSize.megabytes(2));
        assertThat(DataSize.kilobytes(1)).isLessThan(DataSize.gigabytes(2));
        assertThat(DataSize.kilobytes(1)).isLessThan(DataSize.terabytes(2));

        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.bytes(2));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.kilobytes(2));
        assertThat(DataSize.megabytes(1)).isLessThan(DataSize.megabytes(2));
        assertThat(DataSize.megabytes(1)).isLessThan(DataSize.gigabytes(2));
        assertThat(DataSize.megabytes(1)).isLessThan(DataSize.terabytes(2));

        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.bytes(2));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.kilobytes(2));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.megabytes(2));
        assertThat(DataSize.gigabytes(1)).isLessThan(DataSize.gigabytes(2));
        assertThat(DataSize.gigabytes(1)).isLessThan(DataSize.terabytes(2));

        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.bytes(2));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.kilobytes(2));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.megabytes(2));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.gigabytes(2));
        assertThat(DataSize.terabytes(1)).isLessThan(DataSize.terabytes(2));

        assertThat(DataSize.bytes(2)).isGreaterThan(DataSize.bytes(1));
        assertThat(DataSize.bytes(2)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.bytes(2)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.bytes(2)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.bytes(2)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.kilobytes(2)).isGreaterThan(DataSize.bytes(1));
        assertThat(DataSize.kilobytes(2)).isGreaterThan(DataSize.kilobytes(1));
        assertThat(DataSize.kilobytes(2)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.kilobytes(2)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.kilobytes(2)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.megabytes(2)).isGreaterThan(DataSize.bytes(1));
        assertThat(DataSize.megabytes(2)).isGreaterThan(DataSize.kilobytes(1));
        assertThat(DataSize.megabytes(2)).isGreaterThan(DataSize.megabytes(1));
        assertThat(DataSize.megabytes(2)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.megabytes(2)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.gigabytes(2)).isGreaterThan(DataSize.bytes(1));
        assertThat(DataSize.gigabytes(2)).isGreaterThan(DataSize.kilobytes(1));
        assertThat(DataSize.gigabytes(2)).isGreaterThan(DataSize.megabytes(1));
        assertThat(DataSize.gigabytes(2)).isGreaterThan(DataSize.gigabytes(1));
        assertThat(DataSize.gigabytes(2)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.terabytes(2)).isGreaterThan(DataSize.bytes(1));
        assertThat(DataSize.terabytes(2)).isGreaterThan(DataSize.kilobytes(1));
        assertThat(DataSize.terabytes(2)).isGreaterThan(DataSize.megabytes(1));
        assertThat(DataSize.terabytes(2)).isGreaterThan(DataSize.gigabytes(1));
        assertThat(DataSize.terabytes(2)).isGreaterThan(DataSize.terabytes(1));

        // one negative, one positive
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.bytes(-1)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.kilobytes(-1)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.megabytes(-1)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.gigabytes(-1)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.bytes(1));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.kilobytes(1));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.megabytes(1));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.gigabytes(1));
        assertThat(DataSize.terabytes(-1)).isLessThan(DataSize.terabytes(1));

        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.bytes(1)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.kilobytes(1)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.megabytes(1)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.gigabytes(1)).isGreaterThan(DataSize.terabytes(-1));

        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.bytes(-1));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.kilobytes(-1));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.megabytes(-1));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.gigabytes(-1));
        assertThat(DataSize.terabytes(1)).isGreaterThan(DataSize.terabytes(-1));
    }

    @Test
    void serializesCorrectlyWithJackson() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.writeValueAsString(DataSize.bytes(0L))).isEqualTo("\"0 bytes\"");
        assertThat(mapper.writeValueAsString(DataSize.bytes(1L))).isEqualTo("\"1 byte\"");
        assertThat(mapper.writeValueAsString(DataSize.bytes(2L))).isEqualTo("\"2 bytes\"");

        assertThat(mapper.writeValueAsString(DataSize.kilobytes(0L))).isEqualTo("\"0 kilobytes\"");
        assertThat(mapper.writeValueAsString(DataSize.kilobytes(1L))).isEqualTo("\"1 kilobyte\"");
        assertThat(mapper.writeValueAsString(DataSize.kilobytes(2L))).isEqualTo("\"2 kilobytes\"");
        assertThat(mapper.writeValueAsString(DataSize.megabytes(0L))).isEqualTo("\"0 megabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.megabytes(1L))).isEqualTo("\"1 megabyte\"");
        assertThat(mapper.writeValueAsString(DataSize.megabytes(2L))).isEqualTo("\"2 megabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.gigabytes(0L))).isEqualTo("\"0 gigabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.gigabytes(1L))).isEqualTo("\"1 gigabyte\"");
        assertThat(mapper.writeValueAsString(DataSize.gigabytes(2L))).isEqualTo("\"2 gigabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.terabytes(0L))).isEqualTo("\"0 terabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.terabytes(1L))).isEqualTo("\"1 terabyte\"");
        assertThat(mapper.writeValueAsString(DataSize.terabytes(2L))).isEqualTo("\"2 terabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.petabytes(0L))).isEqualTo("\"0 petabytes\"");
        assertThat(mapper.writeValueAsString(DataSize.petabytes(1L))).isEqualTo("\"1 petabyte\"");
        assertThat(mapper.writeValueAsString(DataSize.petabytes(2L))).isEqualTo("\"2 petabytes\"");

        assertThat(mapper.writeValueAsString(DataSize.kibibytes(0L))).isEqualTo("\"0 kibibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.kibibytes(1L))).isEqualTo("\"1 kibibyte\"");
        assertThat(mapper.writeValueAsString(DataSize.kibibytes(2L))).isEqualTo("\"2 kibibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.mebibytes(0L))).isEqualTo("\"0 mebibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.mebibytes(1L))).isEqualTo("\"1 mebibyte\"");
        assertThat(mapper.writeValueAsString(DataSize.mebibytes(2L))).isEqualTo("\"2 mebibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.gibibytes(0L))).isEqualTo("\"0 gibibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.gibibytes(1L))).isEqualTo("\"1 gibibyte\"");
        assertThat(mapper.writeValueAsString(DataSize.gibibytes(2L))).isEqualTo("\"2 gibibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.tebibytes(0L))).isEqualTo("\"0 tebibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.tebibytes(1L))).isEqualTo("\"1 tebibyte\"");
        assertThat(mapper.writeValueAsString(DataSize.tebibytes(2L))).isEqualTo("\"2 tebibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.pebibytes(0L))).isEqualTo("\"0 pebibytes\"");
        assertThat(mapper.writeValueAsString(DataSize.pebibytes(1L))).isEqualTo("\"1 pebibyte\"");
        assertThat(mapper.writeValueAsString(DataSize.pebibytes(2L))).isEqualTo("\"2 pebibytes\"");
    }

    @Test
    void deserializesCorrectlyWithJackson() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.readValue("\"0 bytes\"", DataSize.class)).isEqualTo(DataSize.bytes(0L));
        assertThat(mapper.readValue("\"1 byte\"", DataSize.class)).isEqualTo(DataSize.bytes(1L));
        assertThat(mapper.readValue("\"2 bytes\"", DataSize.class)).isEqualTo(DataSize.bytes(2L));
        assertThat(mapper.readValue("\"0 kilobytes\"", DataSize.class)).isEqualTo(DataSize.kilobytes(0L));
        assertThat(mapper.readValue("\"1 kilobyte\"", DataSize.class)).isEqualTo(DataSize.kilobytes(1L));
        assertThat(mapper.readValue("\"2 kilobytes\"", DataSize.class)).isEqualTo(DataSize.kilobytes(2L));
        assertThat(mapper.readValue("\"0 megabytes\"", DataSize.class)).isEqualTo(DataSize.megabytes(0L));
        assertThat(mapper.readValue("\"1 megabyte\"", DataSize.class)).isEqualTo(DataSize.megabytes(1L));
        assertThat(mapper.readValue("\"2 megabytes\"", DataSize.class)).isEqualTo(DataSize.megabytes(2L));
        assertThat(mapper.readValue("\"0 gigabytes\"", DataSize.class)).isEqualTo(DataSize.gigabytes(0L));
        assertThat(mapper.readValue("\"1 gigabyte\"", DataSize.class)).isEqualTo(DataSize.gigabytes(1L));
        assertThat(mapper.readValue("\"2 gigabytes\"", DataSize.class)).isEqualTo(DataSize.gigabytes(2L));
        assertThat(mapper.readValue("\"0 terabytes\"", DataSize.class)).isEqualTo(DataSize.terabytes(0L));
        assertThat(mapper.readValue("\"1 terabytes\"", DataSize.class)).isEqualTo(DataSize.terabytes(1L));
        assertThat(mapper.readValue("\"2 terabytes\"", DataSize.class)).isEqualTo(DataSize.terabytes(2L));
        assertThat(mapper.readValue("\"0 petabytes\"", DataSize.class)).isEqualTo(DataSize.petabytes(0L));
        assertThat(mapper.readValue("\"1 petabytes\"", DataSize.class)).isEqualTo(DataSize.petabytes(1L));
        assertThat(mapper.readValue("\"2 petabytes\"", DataSize.class)).isEqualTo(DataSize.petabytes(2L));
    }

    @Test
    void testParseWithDefaultDataSizeUnit() {
        assertThat(DataSize.parse("1 MiB", DataSizeUnit.KIBIBYTES)).isEqualTo(DataSize.mebibytes(1L));
        assertThat(DataSize.parse("128", DataSizeUnit.KIBIBYTES)).isEqualTo(DataSize.kibibytes(128L));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testToSize() {
        assertThat(DataSize.bytes(5L).toSize()).isEqualTo(Size.bytes(5L));
        assertThat(DataSize.kilobytes(5L).toSize()).isEqualTo(Size.bytes(5L * 1000L));
        assertThat(DataSize.kibibytes(5L).toSize()).isEqualTo(Size.kilobytes(5L));
        assertThat(DataSize.megabytes(5L).toSize()).isEqualTo(Size.bytes(5L * 1000L * 1000L));
        assertThat(DataSize.mebibytes(5L).toSize()).isEqualTo(Size.megabytes(5L));
        assertThat(DataSize.gigabytes(5L).toSize()).isEqualTo(Size.bytes(5L * 1000L * 1000L * 1000L));
        assertThat(DataSize.gibibytes(5L).toSize()).isEqualTo(Size.gigabytes(5L));
        assertThat(DataSize.terabytes(5L).toSize()).isEqualTo(Size.bytes(5L * 1000L * 1000L * 1000L * 1000L));
        assertThat(DataSize.tebibytes(5L).toSize()).isEqualTo(Size.terabytes(5L));
        assertThat(DataSize.petabytes(5L).toSize()).isEqualTo(Size.bytes(5L * 1000L * 1000L * 1000L * 1000L * 1000L));
        assertThat(DataSize.pebibytes(5L).toSize()).isEqualTo(Size.terabytes(5L * 1024L));
    }

    @SuppressWarnings("deprecation")
    @Test
    void testFromSize() {
        assertThat(DataSize.fromSize(Size.bytes(5L))).isEqualTo(DataSize.bytes(5L));
        assertThat(DataSize.fromSize(Size.kilobytes(5L))).isEqualTo(DataSize.kibibytes(5L));
        assertThat(DataSize.fromSize(Size.megabytes(5L))).isEqualTo(DataSize.mebibytes(5L));
        assertThat(DataSize.fromSize(Size.gigabytes(5L))).isEqualTo(DataSize.gibibytes(5L));
        assertThat(DataSize.fromSize(Size.terabytes(5L))).isEqualTo(DataSize.tebibytes(5L));
    }
}