package io.dropwizard.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class SizeTest {
    @Test
    public void convertsToPetabytes() throws Exception {
        assertThat(Size.petabytes(2).toPetabytes())
            .isEqualTo(2);
    }

    @Test
    public void convertsToTerabytes() throws Exception {
        assertThat(Size.terabytes(2).toTerabytes())
                .isEqualTo(2);
    }

    @Test
    public void convertsToGigabytes() throws Exception {
        assertThat(Size.terabytes(2).toGigabytes())
                .isEqualTo(2000);
    }

    @Test
    public void convertsToMegabytes() throws Exception {
        assertThat(Size.gigabytes(2).toMegabytes())
                .isEqualTo(2000);
    }

    @Test
    public void convertsToKilobytes() throws Exception {
        assertThat(Size.megabytes(2).toKilobytes())
                .isEqualTo(2000);
    }

    @Test
    public void convertsToBytes() throws Exception {
        assertThat(Size.kilobytes(2).toBytes())
                .isEqualTo(2000L);
    }

    @Test
    public void parsesPetabytes() throws Exception {
        assertThat(Size.parse("2P"))
            .isEqualTo(Size.petabytes(2));

        assertThat(Size.parse("2PB"))
            .isEqualTo(Size.petabytes(2));

        assertThat(Size.parse("1 petabyte"))
            .isEqualTo(Size.petabytes(1));

        assertThat(Size.parse("2 petabytes"))
            .isEqualTo(Size.petabytes(2));
    }

    @Test
    public void parsesPebibytes() throws Exception {
        assertThat(Size.parse("2PiB"))
            .isEqualTo(Size.pebibytes(2));

        assertThat(Size.parse("1 pebibyte"))
            .isEqualTo(Size.pebibytes(1));

        assertThat(Size.parse("2 pebibytes"))
            .isEqualTo(Size.pebibytes(2));
    }

    @Test
    public void parsesTerabytes() throws Exception {
        assertThat(Size.parse("2T"))
            .isEqualTo(Size.terabytes(2));

        assertThat(Size.parse("2TB"))
                .isEqualTo(Size.terabytes(2));

        assertThat(Size.parse("1 terabyte"))
                .isEqualTo(Size.terabytes(1));

        assertThat(Size.parse("2 terabytes"))
                .isEqualTo(Size.terabytes(2));
    }

    @Test
    public void parsesTebibytes() throws Exception {
        assertThat(Size.parse("2TiB"))
            .isEqualTo(Size.tebibytes(2));

        assertThat(Size.parse("1 tebibyte"))
            .isEqualTo(Size.tebibytes(1));

        assertThat(Size.parse("2 tebibytes"))
            .isEqualTo(Size.tebibytes(2));
    }

    @Test
    public void parsesGigabytes() throws Exception {
        assertThat(Size.parse("2G"))
            .isEqualTo(Size.gigabytes(2));

        assertThat(Size.parse("2GB"))
                .isEqualTo(Size.gigabytes(2));

        assertThat(Size.parse("1 gigabyte"))
                .isEqualTo(Size.gigabytes(1));

        assertThat(Size.parse("2 gigabytes"))
                .isEqualTo(Size.gigabytes(2));
    }

    @Test
    public void parsesGibibytes() throws Exception {
        assertThat(Size.parse("2GiB"))
            .isEqualTo(Size.gibibytes(2));

        assertThat(Size.parse("1 gibibyte"))
            .isEqualTo(Size.gibibytes(1));

        assertThat(Size.parse("2 gibibytes"))
            .isEqualTo(Size.gibibytes(2));
    }

    @Test
    public void parsesMegabytes() throws Exception {
        assertThat(Size.parse("2M"))
            .isEqualTo(Size.megabytes(2));

        assertThat(Size.parse("2MB"))
                .isEqualTo(Size.megabytes(2));

        assertThat(Size.parse("1 megabyte"))
                .isEqualTo(Size.megabytes(1));

        assertThat(Size.parse("2 megabytes"))
                .isEqualTo(Size.megabytes(2));
    }

    @Test
    public void parsesMebibytes() throws Exception {
        assertThat(Size.parse("2MiB"))
            .isEqualTo(Size.mebibytes(2));

        assertThat(Size.parse("1 mebibyte"))
            .isEqualTo(Size.mebibytes(1));

        assertThat(Size.parse("2 mebibytes"))
            .isEqualTo(Size.mebibytes(2));
    }

    @Test
    public void parsesKilobytes() throws Exception {
        assertThat(Size.parse("2K"))
            .isEqualTo(Size.kilobytes(2));

        assertThat(Size.parse("2KB"))
                .isEqualTo(Size.kilobytes(2));

        assertThat(Size.parse("1 kilobyte"))
                .isEqualTo(Size.kilobytes(1));

        assertThat(Size.parse("2 kilobytes"))
                .isEqualTo(Size.kilobytes(2));
    }

    @Test
    public void parsesKibibytes() throws Exception {
        assertThat(Size.parse("2KiB"))
            .isEqualTo(Size.kibibytes(2));

        assertThat(Size.parse("1 kibibyte"))
            .isEqualTo(Size.kibibytes(1));

        assertThat(Size.parse("2 kibibytes"))
            .isEqualTo(Size.kibibytes(2));
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
    public void parseSizeWithWhiteSpaces() {
        assertThat(Size.parse("64   kilobytes"))
                .isEqualTo(Size.kilobytes(64));
    }

    @Test
    public void parseCaseInsensitive() {
        assertThat(Size.parse("1b")).isEqualTo(Size.parse("1B"));
    }

    @Test
    public void parseSingleLetterSuffix() {
        assertThat(Size.parse("1B")).isEqualTo(Size.bytes(1));
        assertThat(Size.parse("1K")).isEqualTo(Size.kilobytes(1));
        assertThat(Size.parse("1M")).isEqualTo(Size.megabytes(1));
        assertThat(Size.parse("1G")).isEqualTo(Size.gigabytes(1));
        assertThat(Size.parse("1T")).isEqualTo(Size.terabytes(1));
        assertThat(Size.parse("1P")).isEqualTo(Size.petabytes(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableParseWrongSizeCount() {
        Size.parse("three bytes");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableParseWrongSizeUnit() {
        Size.parse("1EB");
    }

    @Test(expected = IllegalArgumentException.class)
    public void unableParseWrongSizeFormat() {
        Size.parse("1 mega byte");
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

    @Test
    public void isComparable() throws Exception {
        // both zero
        assertThat(Size.bytes(0).compareTo(Size.bytes(0))).isEqualTo(0);
        assertThat(Size.bytes(0).compareTo(Size.kilobytes(0))).isEqualTo(0);
        assertThat(Size.bytes(0).compareTo(Size.megabytes(0))).isEqualTo(0);
        assertThat(Size.bytes(0).compareTo(Size.gigabytes(0))).isEqualTo(0);
        assertThat(Size.bytes(0).compareTo(Size.terabytes(0))).isEqualTo(0);

        assertThat(Size.kilobytes(0).compareTo(Size.bytes(0))).isEqualTo(0);
        assertThat(Size.kilobytes(0).compareTo(Size.kilobytes(0))).isEqualTo(0);
        assertThat(Size.kilobytes(0).compareTo(Size.megabytes(0))).isEqualTo(0);
        assertThat(Size.kilobytes(0).compareTo(Size.gigabytes(0))).isEqualTo(0);
        assertThat(Size.kilobytes(0).compareTo(Size.terabytes(0))).isEqualTo(0);

        assertThat(Size.megabytes(0).compareTo(Size.bytes(0))).isEqualTo(0);
        assertThat(Size.megabytes(0).compareTo(Size.kilobytes(0))).isEqualTo(0);
        assertThat(Size.megabytes(0).compareTo(Size.megabytes(0))).isEqualTo(0);
        assertThat(Size.megabytes(0).compareTo(Size.gigabytes(0))).isEqualTo(0);
        assertThat(Size.megabytes(0).compareTo(Size.terabytes(0))).isEqualTo(0);

        assertThat(Size.gigabytes(0).compareTo(Size.bytes(0))).isEqualTo(0);
        assertThat(Size.gigabytes(0).compareTo(Size.kilobytes(0))).isEqualTo(0);
        assertThat(Size.gigabytes(0).compareTo(Size.megabytes(0))).isEqualTo(0);
        assertThat(Size.gigabytes(0).compareTo(Size.gigabytes(0))).isEqualTo(0);
        assertThat(Size.gigabytes(0).compareTo(Size.terabytes(0))).isEqualTo(0);

        assertThat(Size.terabytes(0).compareTo(Size.bytes(0))).isEqualTo(0);
        assertThat(Size.terabytes(0).compareTo(Size.kilobytes(0))).isEqualTo(0);
        assertThat(Size.terabytes(0).compareTo(Size.megabytes(0))).isEqualTo(0);
        assertThat(Size.terabytes(0).compareTo(Size.gigabytes(0))).isEqualTo(0);
        assertThat(Size.terabytes(0).compareTo(Size.terabytes(0))).isEqualTo(0);

        // one zero, one negative
        assertThat(Size.bytes(0)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.bytes(0)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.bytes(0)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.bytes(0)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.bytes(0)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.kilobytes(0)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.kilobytes(0)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.kilobytes(0)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.kilobytes(0)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.kilobytes(0)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.megabytes(0)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.megabytes(0)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.megabytes(0)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.megabytes(0)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.megabytes(0)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.gigabytes(0)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.gigabytes(0)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.gigabytes(0)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.gigabytes(0)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.gigabytes(0)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.terabytes(0)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.terabytes(0)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.terabytes(0)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.terabytes(0)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.terabytes(0)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.bytes(-1)).isLessThan(Size.bytes(0));
        assertThat(Size.bytes(-1)).isLessThan(Size.kilobytes(0));
        assertThat(Size.bytes(-1)).isLessThan(Size.megabytes(0));
        assertThat(Size.bytes(-1)).isLessThan(Size.gigabytes(0));
        assertThat(Size.bytes(-1)).isLessThan(Size.terabytes(0));

        assertThat(Size.kilobytes(-1)).isLessThan(Size.bytes(0));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.kilobytes(0));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.megabytes(0));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.gigabytes(0));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.terabytes(0));

        assertThat(Size.megabytes(-1)).isLessThan(Size.bytes(0));
        assertThat(Size.megabytes(-1)).isLessThan(Size.kilobytes(0));
        assertThat(Size.megabytes(-1)).isLessThan(Size.megabytes(0));
        assertThat(Size.megabytes(-1)).isLessThan(Size.gigabytes(0));
        assertThat(Size.megabytes(-1)).isLessThan(Size.terabytes(0));

        assertThat(Size.gigabytes(-1)).isLessThan(Size.bytes(0));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.kilobytes(0));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.megabytes(0));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.gigabytes(0));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.terabytes(0));

        assertThat(Size.terabytes(-1)).isLessThan(Size.bytes(0));
        assertThat(Size.terabytes(-1)).isLessThan(Size.kilobytes(0));
        assertThat(Size.terabytes(-1)).isLessThan(Size.megabytes(0));
        assertThat(Size.terabytes(-1)).isLessThan(Size.gigabytes(0));
        assertThat(Size.terabytes(-1)).isLessThan(Size.terabytes(0));

        // one zero, one positive
        assertThat(Size.bytes(0)).isLessThan(Size.bytes(1));
        assertThat(Size.bytes(0)).isLessThan(Size.kilobytes(1));
        assertThat(Size.bytes(0)).isLessThan(Size.megabytes(1));
        assertThat(Size.bytes(0)).isLessThan(Size.gigabytes(1));
        assertThat(Size.bytes(0)).isLessThan(Size.terabytes(1));

        assertThat(Size.kilobytes(0)).isLessThan(Size.bytes(1));
        assertThat(Size.kilobytes(0)).isLessThan(Size.kilobytes(1));
        assertThat(Size.kilobytes(0)).isLessThan(Size.megabytes(1));
        assertThat(Size.kilobytes(0)).isLessThan(Size.gigabytes(1));
        assertThat(Size.kilobytes(0)).isLessThan(Size.terabytes(1));

        assertThat(Size.megabytes(0)).isLessThan(Size.bytes(1));
        assertThat(Size.megabytes(0)).isLessThan(Size.kilobytes(1));
        assertThat(Size.megabytes(0)).isLessThan(Size.megabytes(1));
        assertThat(Size.megabytes(0)).isLessThan(Size.gigabytes(1));
        assertThat(Size.megabytes(0)).isLessThan(Size.terabytes(1));

        assertThat(Size.gigabytes(0)).isLessThan(Size.bytes(1));
        assertThat(Size.gigabytes(0)).isLessThan(Size.kilobytes(1));
        assertThat(Size.gigabytes(0)).isLessThan(Size.megabytes(1));
        assertThat(Size.gigabytes(0)).isLessThan(Size.gigabytes(1));
        assertThat(Size.gigabytes(0)).isLessThan(Size.terabytes(1));

        assertThat(Size.terabytes(0)).isLessThan(Size.bytes(1));
        assertThat(Size.terabytes(0)).isLessThan(Size.kilobytes(1));
        assertThat(Size.terabytes(0)).isLessThan(Size.megabytes(1));
        assertThat(Size.terabytes(0)).isLessThan(Size.gigabytes(1));
        assertThat(Size.terabytes(0)).isLessThan(Size.terabytes(1));

        assertThat(Size.bytes(1)).isGreaterThan(Size.bytes(0));
        assertThat(Size.bytes(1)).isGreaterThan(Size.kilobytes(0));
        assertThat(Size.bytes(1)).isGreaterThan(Size.megabytes(0));
        assertThat(Size.bytes(1)).isGreaterThan(Size.gigabytes(0));
        assertThat(Size.bytes(1)).isGreaterThan(Size.terabytes(0));

        assertThat(Size.kilobytes(1)).isGreaterThan(Size.bytes(0));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.kilobytes(0));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.megabytes(0));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.gigabytes(0));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.terabytes(0));

        assertThat(Size.megabytes(1)).isGreaterThan(Size.bytes(0));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.kilobytes(0));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.megabytes(0));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.gigabytes(0));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.terabytes(0));

        assertThat(Size.gigabytes(1)).isGreaterThan(Size.bytes(0));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.kilobytes(0));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.megabytes(0));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.gigabytes(0));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.terabytes(0));

        assertThat(Size.terabytes(1)).isGreaterThan(Size.bytes(0));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.kilobytes(0));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.megabytes(0));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.gigabytes(0));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.terabytes(0));

        // both negative
        assertThat(Size.bytes(-2)).isLessThan(Size.bytes(-1));
        assertThat(Size.bytes(-2)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.bytes(-2)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.bytes(-2)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.bytes(-2)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.kilobytes(-2)).isLessThan(Size.bytes(-1));
        assertThat(Size.kilobytes(-2)).isLessThan(Size.kilobytes(-1));
        assertThat(Size.kilobytes(-2)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.kilobytes(-2)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.kilobytes(-2)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.megabytes(-2)).isLessThan(Size.bytes(-1));
        assertThat(Size.megabytes(-2)).isLessThan(Size.kilobytes(-1));
        assertThat(Size.megabytes(-2)).isLessThan(Size.megabytes(-1));
        assertThat(Size.megabytes(-2)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.megabytes(-2)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.gigabytes(-2)).isLessThan(Size.bytes(-1));
        assertThat(Size.gigabytes(-2)).isLessThan(Size.kilobytes(-1));
        assertThat(Size.gigabytes(-2)).isLessThan(Size.megabytes(-1));
        assertThat(Size.gigabytes(-2)).isLessThan(Size.gigabytes(-1));
        assertThat(Size.gigabytes(-2)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.terabytes(-2)).isLessThan(Size.bytes(-1));
        assertThat(Size.terabytes(-2)).isLessThan(Size.kilobytes(-1));
        assertThat(Size.terabytes(-2)).isLessThan(Size.megabytes(-1));
        assertThat(Size.terabytes(-2)).isLessThan(Size.gigabytes(-1));
        assertThat(Size.terabytes(-2)).isLessThan(Size.terabytes(-1));

        assertThat(Size.bytes(-1)).isGreaterThan(Size.bytes(-2));
        assertThat(Size.bytes(-1)).isGreaterThan(Size.kilobytes(-2));
        assertThat(Size.bytes(-1)).isGreaterThan(Size.megabytes(-2));
        assertThat(Size.bytes(-1)).isGreaterThan(Size.gigabytes(-2));
        assertThat(Size.bytes(-1)).isGreaterThan(Size.terabytes(-2));

        assertThat(Size.kilobytes(-1)).isLessThan(Size.bytes(-2));
        assertThat(Size.kilobytes(-1)).isGreaterThan(Size.kilobytes(-2));
        assertThat(Size.kilobytes(-1)).isGreaterThan(Size.megabytes(-2));
        assertThat(Size.kilobytes(-1)).isGreaterThan(Size.gigabytes(-2));
        assertThat(Size.kilobytes(-1)).isGreaterThan(Size.terabytes(-2));

        assertThat(Size.megabytes(-1)).isLessThan(Size.bytes(-2));
        assertThat(Size.megabytes(-1)).isLessThan(Size.kilobytes(-2));
        assertThat(Size.megabytes(-1)).isGreaterThan(Size.megabytes(-2));
        assertThat(Size.megabytes(-1)).isGreaterThan(Size.gigabytes(-2));
        assertThat(Size.megabytes(-1)).isGreaterThan(Size.terabytes(-2));

        assertThat(Size.gigabytes(-1)).isLessThan(Size.bytes(-2));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.kilobytes(-2));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.megabytes(-2));
        assertThat(Size.gigabytes(-1)).isGreaterThan(Size.gigabytes(-2));
        assertThat(Size.gigabytes(-1)).isGreaterThan(Size.terabytes(-2));

        assertThat(Size.terabytes(-1)).isLessThan(Size.bytes(-2));
        assertThat(Size.terabytes(-1)).isLessThan(Size.kilobytes(-2));
        assertThat(Size.terabytes(-1)).isLessThan(Size.megabytes(-2));
        assertThat(Size.terabytes(-1)).isLessThan(Size.gigabytes(-2));
        assertThat(Size.terabytes(-1)).isGreaterThan(Size.terabytes(-2));

        // both positive
        assertThat(Size.bytes(1)).isLessThan(Size.bytes(2));
        assertThat(Size.bytes(1)).isLessThan(Size.kilobytes(2));
        assertThat(Size.bytes(1)).isLessThan(Size.megabytes(2));
        assertThat(Size.bytes(1)).isLessThan(Size.gigabytes(2));
        assertThat(Size.bytes(1)).isLessThan(Size.terabytes(2));

        assertThat(Size.kilobytes(1)).isGreaterThan(Size.bytes(2));
        assertThat(Size.kilobytes(1)).isLessThan(Size.kilobytes(2));
        assertThat(Size.kilobytes(1)).isLessThan(Size.megabytes(2));
        assertThat(Size.kilobytes(1)).isLessThan(Size.gigabytes(2));
        assertThat(Size.kilobytes(1)).isLessThan(Size.terabytes(2));

        assertThat(Size.megabytes(1)).isGreaterThan(Size.bytes(2));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.kilobytes(2));
        assertThat(Size.megabytes(1)).isLessThan(Size.megabytes(2));
        assertThat(Size.megabytes(1)).isLessThan(Size.gigabytes(2));
        assertThat(Size.megabytes(1)).isLessThan(Size.terabytes(2));

        assertThat(Size.gigabytes(1)).isGreaterThan(Size.bytes(2));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.kilobytes(2));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.megabytes(2));
        assertThat(Size.gigabytes(1)).isLessThan(Size.gigabytes(2));
        assertThat(Size.gigabytes(1)).isLessThan(Size.terabytes(2));

        assertThat(Size.terabytes(1)).isGreaterThan(Size.bytes(2));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.kilobytes(2));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.megabytes(2));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.gigabytes(2));
        assertThat(Size.terabytes(1)).isLessThan(Size.terabytes(2));

        assertThat(Size.bytes(2)).isGreaterThan(Size.bytes(1));
        assertThat(Size.bytes(2)).isLessThan(Size.kilobytes(1));
        assertThat(Size.bytes(2)).isLessThan(Size.megabytes(1));
        assertThat(Size.bytes(2)).isLessThan(Size.gigabytes(1));
        assertThat(Size.bytes(2)).isLessThan(Size.terabytes(1));

        assertThat(Size.kilobytes(2)).isGreaterThan(Size.bytes(1));
        assertThat(Size.kilobytes(2)).isGreaterThan(Size.kilobytes(1));
        assertThat(Size.kilobytes(2)).isLessThan(Size.megabytes(1));
        assertThat(Size.kilobytes(2)).isLessThan(Size.gigabytes(1));
        assertThat(Size.kilobytes(2)).isLessThan(Size.terabytes(1));

        assertThat(Size.megabytes(2)).isGreaterThan(Size.bytes(1));
        assertThat(Size.megabytes(2)).isGreaterThan(Size.kilobytes(1));
        assertThat(Size.megabytes(2)).isGreaterThan(Size.megabytes(1));
        assertThat(Size.megabytes(2)).isLessThan(Size.gigabytes(1));
        assertThat(Size.megabytes(2)).isLessThan(Size.terabytes(1));

        assertThat(Size.gigabytes(2)).isGreaterThan(Size.bytes(1));
        assertThat(Size.gigabytes(2)).isGreaterThan(Size.kilobytes(1));
        assertThat(Size.gigabytes(2)).isGreaterThan(Size.megabytes(1));
        assertThat(Size.gigabytes(2)).isGreaterThan(Size.gigabytes(1));
        assertThat(Size.gigabytes(2)).isLessThan(Size.terabytes(1));

        assertThat(Size.terabytes(2)).isGreaterThan(Size.bytes(1));
        assertThat(Size.terabytes(2)).isGreaterThan(Size.kilobytes(1));
        assertThat(Size.terabytes(2)).isGreaterThan(Size.megabytes(1));
        assertThat(Size.terabytes(2)).isGreaterThan(Size.gigabytes(1));
        assertThat(Size.terabytes(2)).isGreaterThan(Size.terabytes(1));

        // one negative, one positive
        assertThat(Size.bytes(-1)).isLessThan(Size.bytes(1));
        assertThat(Size.bytes(-1)).isLessThan(Size.kilobytes(1));
        assertThat(Size.bytes(-1)).isLessThan(Size.megabytes(1));
        assertThat(Size.bytes(-1)).isLessThan(Size.gigabytes(1));
        assertThat(Size.bytes(-1)).isLessThan(Size.terabytes(1));

        assertThat(Size.kilobytes(-1)).isLessThan(Size.bytes(1));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.kilobytes(1));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.megabytes(1));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.gigabytes(1));
        assertThat(Size.kilobytes(-1)).isLessThan(Size.terabytes(1));

        assertThat(Size.megabytes(-1)).isLessThan(Size.bytes(1));
        assertThat(Size.megabytes(-1)).isLessThan(Size.kilobytes(1));
        assertThat(Size.megabytes(-1)).isLessThan(Size.megabytes(1));
        assertThat(Size.megabytes(-1)).isLessThan(Size.gigabytes(1));
        assertThat(Size.megabytes(-1)).isLessThan(Size.terabytes(1));

        assertThat(Size.gigabytes(-1)).isLessThan(Size.bytes(1));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.kilobytes(1));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.megabytes(1));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.gigabytes(1));
        assertThat(Size.gigabytes(-1)).isLessThan(Size.terabytes(1));

        assertThat(Size.terabytes(-1)).isLessThan(Size.bytes(1));
        assertThat(Size.terabytes(-1)).isLessThan(Size.kilobytes(1));
        assertThat(Size.terabytes(-1)).isLessThan(Size.megabytes(1));
        assertThat(Size.terabytes(-1)).isLessThan(Size.gigabytes(1));
        assertThat(Size.terabytes(-1)).isLessThan(Size.terabytes(1));

        assertThat(Size.bytes(1)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.bytes(1)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.bytes(1)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.bytes(1)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.bytes(1)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.kilobytes(1)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.kilobytes(1)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.megabytes(1)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.megabytes(1)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.gigabytes(1)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.gigabytes(1)).isGreaterThan(Size.terabytes(-1));

        assertThat(Size.terabytes(1)).isGreaterThan(Size.bytes(-1));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.kilobytes(-1));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.megabytes(-1));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.gigabytes(-1));
        assertThat(Size.terabytes(1)).isGreaterThan(Size.terabytes(-1));
    }

    @Test
    public void serializesCorrectlyWithJackson() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.writeValueAsString(Size.bytes(0L))).isEqualTo("\"0 bytes\"");
        assertThat(mapper.writeValueAsString(Size.bytes(1L))).isEqualTo("\"1 byte\"");
        assertThat(mapper.writeValueAsString(Size.bytes(2L))).isEqualTo("\"2 bytes\"");

        assertThat(mapper.writeValueAsString(Size.kilobytes(0L))).isEqualTo("\"0 kilobytes\"");
        assertThat(mapper.writeValueAsString(Size.kilobytes(1L))).isEqualTo("\"1 kilobyte\"");
        assertThat(mapper.writeValueAsString(Size.kilobytes(2L))).isEqualTo("\"2 kilobytes\"");
        assertThat(mapper.writeValueAsString(Size.megabytes(0L))).isEqualTo("\"0 megabytes\"");
        assertThat(mapper.writeValueAsString(Size.megabytes(1L))).isEqualTo("\"1 megabyte\"");
        assertThat(mapper.writeValueAsString(Size.megabytes(2L))).isEqualTo("\"2 megabytes\"");
        assertThat(mapper.writeValueAsString(Size.gigabytes(0L))).isEqualTo("\"0 gigabytes\"");
        assertThat(mapper.writeValueAsString(Size.gigabytes(1L))).isEqualTo("\"1 gigabyte\"");
        assertThat(mapper.writeValueAsString(Size.gigabytes(2L))).isEqualTo("\"2 gigabytes\"");
        assertThat(mapper.writeValueAsString(Size.terabytes(0L))).isEqualTo("\"0 terabytes\"");
        assertThat(mapper.writeValueAsString(Size.terabytes(1L))).isEqualTo("\"1 terabyte\"");
        assertThat(mapper.writeValueAsString(Size.terabytes(2L))).isEqualTo("\"2 terabytes\"");
        assertThat(mapper.writeValueAsString(Size.petabytes(0L))).isEqualTo("\"0 petabytes\"");
        assertThat(mapper.writeValueAsString(Size.petabytes(1L))).isEqualTo("\"1 petabyte\"");
        assertThat(mapper.writeValueAsString(Size.petabytes(2L))).isEqualTo("\"2 petabytes\"");

        assertThat(mapper.writeValueAsString(Size.kibibytes(0L))).isEqualTo("\"0 kibibytes\"");
        assertThat(mapper.writeValueAsString(Size.kibibytes(1L))).isEqualTo("\"1 kibibyte\"");
        assertThat(mapper.writeValueAsString(Size.kibibytes(2L))).isEqualTo("\"2 kibibytes\"");
        assertThat(mapper.writeValueAsString(Size.mebibytes(0L))).isEqualTo("\"0 mebibytes\"");
        assertThat(mapper.writeValueAsString(Size.mebibytes(1L))).isEqualTo("\"1 mebibyte\"");
        assertThat(mapper.writeValueAsString(Size.mebibytes(2L))).isEqualTo("\"2 mebibytes\"");
        assertThat(mapper.writeValueAsString(Size.gibibytes(0L))).isEqualTo("\"0 gibibytes\"");
        assertThat(mapper.writeValueAsString(Size.gibibytes(1L))).isEqualTo("\"1 gibibyte\"");
        assertThat(mapper.writeValueAsString(Size.gibibytes(2L))).isEqualTo("\"2 gibibytes\"");
        assertThat(mapper.writeValueAsString(Size.tebibytes(0L))).isEqualTo("\"0 tebibytes\"");
        assertThat(mapper.writeValueAsString(Size.tebibytes(1L))).isEqualTo("\"1 tebibyte\"");
        assertThat(mapper.writeValueAsString(Size.tebibytes(2L))).isEqualTo("\"2 tebibytes\"");
        assertThat(mapper.writeValueAsString(Size.pebibytes(0L))).isEqualTo("\"0 pebibytes\"");
        assertThat(mapper.writeValueAsString(Size.pebibytes(1L))).isEqualTo("\"1 pebibyte\"");
        assertThat(mapper.writeValueAsString(Size.pebibytes(2L))).isEqualTo("\"2 pebibytes\"");
    }

    @Test
    public void deserializesCorrectlyWithJackson() throws IOException {
        final ObjectMapper mapper = new ObjectMapper();

        assertThat(mapper.readValue("\"0 bytes\"", Size.class)).isEqualTo(Size.bytes(0L));
        assertThat(mapper.readValue("\"1 byte\"", Size.class)).isEqualTo(Size.bytes(1L));
        assertThat(mapper.readValue("\"2 bytes\"", Size.class)).isEqualTo(Size.bytes(2L));
        assertThat(mapper.readValue("\"0 kilobytes\"", Size.class)).isEqualTo(Size.kilobytes(0L));
        assertThat(mapper.readValue("\"1 kilobyte\"", Size.class)).isEqualTo(Size.kilobytes(1L));
        assertThat(mapper.readValue("\"2 kilobytes\"", Size.class)).isEqualTo(Size.kilobytes(2L));
        assertThat(mapper.readValue("\"0 megabytes\"", Size.class)).isEqualTo(Size.megabytes(0L));
        assertThat(mapper.readValue("\"1 megabyte\"", Size.class)).isEqualTo(Size.megabytes(1L));
        assertThat(mapper.readValue("\"2 megabytes\"", Size.class)).isEqualTo(Size.megabytes(2L));
        assertThat(mapper.readValue("\"0 gigabytes\"", Size.class)).isEqualTo(Size.gigabytes(0L));
        assertThat(mapper.readValue("\"1 gigabyte\"", Size.class)).isEqualTo(Size.gigabytes(1L));
        assertThat(mapper.readValue("\"2 gigabytes\"", Size.class)).isEqualTo(Size.gigabytes(2L));
        assertThat(mapper.readValue("\"0 terabytes\"", Size.class)).isEqualTo(Size.terabytes(0L));
        assertThat(mapper.readValue("\"1 terabytes\"", Size.class)).isEqualTo(Size.terabytes(1L));
        assertThat(mapper.readValue("\"2 terabytes\"", Size.class)).isEqualTo(Size.terabytes(2L));
        assertThat(mapper.readValue("\"0 petabytes\"", Size.class)).isEqualTo(Size.petabytes(0L));
        assertThat(mapper.readValue("\"1 petabytes\"", Size.class)).isEqualTo(Size.petabytes(1L));
        assertThat(mapper.readValue("\"2 petabytes\"", Size.class)).isEqualTo(Size.petabytes(2L));
    }
}
