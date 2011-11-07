package com.yammer.dropwizard.util.tests;

import com.yammer.dropwizard.util.Size;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class SizeTest {
    @Test
    public void convertsToTerabytes() throws Exception {
        assertThat(Size.terabytes(2).toTerabytes(),
                   is(2L));
    }

    @Test
    public void convertsToGigabytes() throws Exception {
        assertThat(Size.terabytes(2).toGigabytes(),
                   is(2048L));
    }

    @Test
    public void convertsToMegabytes() throws Exception {
        assertThat(Size.gigabytes(2).toMegabytes(),
                   is(2048L));
    }

    @Test
    public void convertsToKilobytes() throws Exception {
        assertThat(Size.megabytes(2).toKilobytes(),
                   is(2048L));
    }

    @Test
    public void convertsToBytes() throws Exception {
        assertThat(Size.kilobytes(2).toBytes(),
                   is(2048L));
    }

    @Test
    public void parsesTerabytes() throws Exception {
        assertThat(new Size("2TB"),
                   is(Size.terabytes(2)));

        assertThat(new Size("2TiB"),
                   is(Size.terabytes(2)));
        
        assertThat(new Size("1 terabyte"),
                   is(Size.terabytes(1)));

        assertThat(new Size("2 terabytes"),
                   is(Size.terabytes(2)));
    }

    @Test
    public void parsesGigabytes() throws Exception {
        assertThat(new Size("2GB"),
                   is(Size.gigabytes(2)));

        assertThat(new Size("2GiB"),
                   is(Size.gigabytes(2)));

        assertThat(new Size("1 gigabyte"),
                   is(Size.gigabytes(1)));

        assertThat(new Size("2 gigabytes"),
                   is(Size.gigabytes(2)));
    }

    @Test
    public void parsesMegabytes() throws Exception {
        assertThat(new Size("2MB"),
                   is(Size.megabytes(2)));

        assertThat(new Size("2MiB"),
                   is(Size.megabytes(2)));

        assertThat(new Size("1 megabyte"),
                   is(Size.megabytes(1)));

        assertThat(new Size("2 megabytes"),
                   is(Size.megabytes(2)));
    }

    @Test
    public void parsesKilobytes() throws Exception {
        assertThat(new Size("2KB"),
                   is(Size.kilobytes(2)));

        assertThat(new Size("2KiB"),
                   is(Size.kilobytes(2)));

        assertThat(new Size("1 kilobyte"),
                   is(Size.kilobytes(1)));

        assertThat(new Size("2 kilobytes"),
                   is(Size.kilobytes(2)));
    }

    @Test
    public void parsesBytes() throws Exception {
        assertThat(new Size("2B"),
                   is(Size.bytes(2)));

        assertThat(new Size("1 byte"),
                   is(Size.bytes(1)));

        assertThat(new Size("2 bytes"),
                   is(Size.bytes(2)));
    }

    @Test
    public void isHumanReadable() throws Exception {
        assertThat(Size.gigabytes(3).toString(),
                   is("3 gigabytes"));

        assertThat(Size.kilobytes(1).toString(),
                   is("1 kilobyte"));
    }
}
