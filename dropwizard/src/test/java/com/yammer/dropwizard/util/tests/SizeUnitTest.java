package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.SizeUnit;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeUnitTest {
    // BYTES

    @Test
    public void oneByteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.BYTES),
                   is(1L));

        assertThat(SizeUnit.BYTES.toBytes(1),
                   is(1L));
    }


    @Test
    public void oneByteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.BYTES),
                   is(0L));

        assertThat(SizeUnit.BYTES.toKilobytes(1),
                   is(0L));
    }

    @Test
    public void oneByteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.BYTES),
                   is(0L));

        assertThat(SizeUnit.BYTES.toMegabytes(1),
                   is(0L));
    }

    @Test
    public void oneByteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.BYTES),
                   is(0L));

        assertThat(SizeUnit.BYTES.toGigabytes(1),
                   is(0L));
    }

    @Test
    public void oneByteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.BYTES),
                   is(0L));

        assertThat(SizeUnit.BYTES.toTerabytes(1),
                   is(0L));
    }

    // KILOBYTES

    @Test
    public void oneKilobyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.KILOBYTES),
                   is(1024L));
        
        assertThat(SizeUnit.KILOBYTES.toBytes(1),
                   is(1024L));
    }

    @Test
    public void oneKilobyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.KILOBYTES),
                   is(1L));

        assertThat(SizeUnit.KILOBYTES.toKilobytes(1),
                   is(1L));
    }

    @Test
    public void oneKilobyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.KILOBYTES),
                   is(0L));

        assertThat(SizeUnit.KILOBYTES.toMegabytes(1),
                   is(0L));
    }

    @Test
    public void oneKilobyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.KILOBYTES),
                   is(0L));

        assertThat(SizeUnit.KILOBYTES.toGigabytes(1),
                   is(0L));
    }

    @Test
    public void oneKilobyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.KILOBYTES),
                   is(0L));

        assertThat(SizeUnit.KILOBYTES.toTerabytes(1),
                   is(0L));
    }

    // MEGABYTES

    @Test
    public void oneMegabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.MEGABYTES),
                   is(1048576L));

        assertThat(SizeUnit.MEGABYTES.toBytes(1),
                   is(1048576L));
    }

    @Test
    public void oneMegabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.MEGABYTES),
                   is(1024L));

        assertThat(SizeUnit.MEGABYTES.toKilobytes(1),
                   is(1024L));
    }

    @Test
    public void oneMegabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.MEGABYTES),
                   is(1L));

        assertThat(SizeUnit.MEGABYTES.toMegabytes(1),
                   is(1L));
    }

    @Test
    public void oneMegabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.MEGABYTES),
                   is(0L));

        assertThat(SizeUnit.MEGABYTES.toGigabytes(1),
                   is(0L));
    }

    @Test
    public void oneMegabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.MEGABYTES),
                   is(0L));

        assertThat(SizeUnit.MEGABYTES.toTerabytes(1),
                   is(0L));
    }

    // GIGABYTES

    @Test
    public void oneGigabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.GIGABYTES),
                   is(1073741824L));

        assertThat(SizeUnit.GIGABYTES.toBytes(1),
                   is(1073741824L));
    }

    @Test
    public void oneGigabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.GIGABYTES),
                   is(1048576L));

        assertThat(SizeUnit.GIGABYTES.toKilobytes(1),
                   is(1048576L));
    }

    @Test
    public void oneGigabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.GIGABYTES),
                   is(1024L));

        assertThat(SizeUnit.GIGABYTES.toMegabytes(1),
                   is(1024L));
    }

    @Test
    public void oneGigabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.GIGABYTES),
                   is(1L));

        assertThat(SizeUnit.GIGABYTES.toGigabytes(1),
                   is(1L));
    }

    @Test
    public void oneGigabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.GIGABYTES),
                   is(0L));

        assertThat(SizeUnit.GIGABYTES.toTerabytes(1),
                   is(0L));
    }

    // TERABYTES

    @Test
    public void oneTerabyteInBytes() throws Exception {
        assertThat(SizeUnit.BYTES.convert(1, SizeUnit.TERABYTES),
                   is(1099511627776L));

        assertThat(SizeUnit.TERABYTES.toBytes(1),
                   is(1099511627776L));
    }

    @Test
    public void oneTerabyteInKilobytes() throws Exception {
        assertThat(SizeUnit.KILOBYTES.convert(1, SizeUnit.TERABYTES),
                   is(1073741824L));

        assertThat(SizeUnit.TERABYTES.toKilobytes(1),
                   is(1073741824L));
    }

    @Test
    public void oneTerabyteInMegabytes() throws Exception {
        assertThat(SizeUnit.MEGABYTES.convert(1, SizeUnit.TERABYTES),
                   is(1048576L));

        assertThat(SizeUnit.TERABYTES.toMegabytes(1),
                   is(1048576L));
    }

    @Test
    public void oneTerabyteInGigabytes() throws Exception {
        assertThat(SizeUnit.GIGABYTES.convert(1, SizeUnit.TERABYTES),
                   is(1024L));

        assertThat(SizeUnit.TERABYTES.toGigabytes(1),
                   is(1024L));
    }

    @Test
    public void oneTerabyteInTerabytes() throws Exception {
        assertThat(SizeUnit.TERABYTES.convert(1, SizeUnit.TERABYTES),
                   is(1L));

        assertThat(SizeUnit.TERABYTES.toTerabytes(1),
                   is(1L));
    }
}
