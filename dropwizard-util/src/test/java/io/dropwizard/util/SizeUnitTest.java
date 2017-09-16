package io.dropwizard.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Parameterized.class)
public class SizeUnitTest {

    private SizeUnit src;
    private SizeUnit dst;
    private Converter srcConverter;
    private long convertedValue;

    private static class Converter {
        private Function<Long, Long> f;

        Converter(Function<Long, Long> f) {
            this.f = f;
        }

        long toOneDst() {
            return f.apply(1L);
        }
    }

    public SizeUnitTest(SizeUnit src, SizeUnit dst, Converter c, long value) {
        super();
        this.src = src;
        this.dst = dst;
        this.srcConverter = c;
        this.convertedValue = value;
    }

    @Parameters
    public static Collection parameters() {
        return Arrays.asList(new Object[][] {
            // bytes
            {SizeUnit.BYTES, SizeUnit.BYTES, new Converter(SizeUnit.BYTES::toBytes), 1L},
            {SizeUnit.BYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.BYTES::toKilobytes), 0L},
            {SizeUnit.BYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.BYTES::toMegabytes), 0L},
            {SizeUnit.BYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.BYTES::toGigabytes), 0L},
            {SizeUnit.BYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.BYTES::toTerabytes), 0L},
            {SizeUnit.BYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.BYTES::toPetabytes), 0L},

            {SizeUnit.BYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.BYTES::toKibibytes), 0L},
            {SizeUnit.BYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.BYTES::toMebibytes), 0L},
            {SizeUnit.BYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.BYTES::toGibibytes), 0L},
            {SizeUnit.BYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.BYTES::toTebibytes), 0L},
            {SizeUnit.BYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.BYTES::toPebibytes), 0L},

            // kilobytes
            {SizeUnit.KILOBYTES, SizeUnit.BYTES, new Converter(SizeUnit.KILOBYTES::toBytes), 1000L},
            {SizeUnit.KILOBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.KILOBYTES::toKilobytes), 1L},
            {SizeUnit.KILOBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.KILOBYTES::toMegabytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.KILOBYTES::toGigabytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.KILOBYTES::toTerabytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.KILOBYTES::toPetabytes), 0L},

            {SizeUnit.KILOBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.KILOBYTES::toKibibytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.KILOBYTES::toMebibytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.KILOBYTES::toGibibytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.KILOBYTES::toTebibytes), 0L},
            {SizeUnit.KILOBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.KILOBYTES::toPebibytes), 0L},

            // megabytes
            {SizeUnit.MEGABYTES, SizeUnit.BYTES, new Converter(SizeUnit.MEGABYTES::toBytes), 1000L * 1000},
            {SizeUnit.MEGABYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.MEGABYTES::toKilobytes), 1000L},
            {SizeUnit.MEGABYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.MEGABYTES::toMegabytes), 1L},
            {SizeUnit.MEGABYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.MEGABYTES::toGigabytes), 0L},
            {SizeUnit.MEGABYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.MEGABYTES::toTerabytes), 0L},
            {SizeUnit.MEGABYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.MEGABYTES::toPetabytes), 0L},

            {SizeUnit.MEGABYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.MEGABYTES::toKibibytes), 1000L * 1000 / 1024},
            {SizeUnit.MEGABYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.MEGABYTES::toMebibytes), 0L},
            {SizeUnit.MEGABYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.MEGABYTES::toGibibytes), 0L},
            {SizeUnit.MEGABYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.MEGABYTES::toTebibytes), 0L},
            {SizeUnit.MEGABYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.MEGABYTES::toPebibytes), 0L},

            // gigabytes
            {SizeUnit.GIGABYTES, SizeUnit.BYTES, new Converter(SizeUnit.GIGABYTES::toBytes), 1000L * 1000 * 1000},
            {SizeUnit.GIGABYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.GIGABYTES::toKilobytes), 1000L * 1000},
            {SizeUnit.GIGABYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.GIGABYTES::toMegabytes), 1000L},
            {SizeUnit.GIGABYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.GIGABYTES::toGigabytes), 1L},
            {SizeUnit.GIGABYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.GIGABYTES::toTerabytes), 0L},
            {SizeUnit.GIGABYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.GIGABYTES::toPetabytes), 0L},

            {SizeUnit.GIGABYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.GIGABYTES::toKibibytes), 1000L * 1000 * 1000 / 1024},
            {SizeUnit.GIGABYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.GIGABYTES::toMebibytes), 1000L * 1000 * 1000 / (1024L * 1024)},
            {SizeUnit.GIGABYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.GIGABYTES::toGibibytes), 0L},
            {SizeUnit.GIGABYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.GIGABYTES::toTebibytes), 0L},
            {SizeUnit.GIGABYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.GIGABYTES::toPebibytes), 0L},

            // terabytes
            {SizeUnit.TERABYTES, SizeUnit.BYTES, new Converter(SizeUnit.TERABYTES::toBytes), 1000L * 1000 * 1000 * 1000},
            {SizeUnit.TERABYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.TERABYTES::toKilobytes), 1000L * 1000 * 1000},
            {SizeUnit.TERABYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.TERABYTES::toMegabytes), 1000L * 1000},
            {SizeUnit.TERABYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.TERABYTES::toGigabytes), 1000L},
            {SizeUnit.TERABYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.TERABYTES::toTerabytes), 1L},
            {SizeUnit.TERABYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.TERABYTES::toPetabytes), 0L},

            {SizeUnit.TERABYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.TERABYTES::toKibibytes), 1000L * 1000 * 1000 * 1000 / 1024},
            {SizeUnit.TERABYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.TERABYTES::toMebibytes), 1000L * 1000 * 1000 * 1000 / (1024L * 1024)},
            {SizeUnit.TERABYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.TERABYTES::toGibibytes), 1000L * 1000 * 1000 * 1000 / (1024L * 1024 * 1024)},
            {SizeUnit.TERABYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.TERABYTES::toTebibytes), 0L},
            {SizeUnit.TERABYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.TERABYTES::toPebibytes), 0L},

            // petabytes
            {SizeUnit.PETABYTES, SizeUnit.BYTES, new Converter(SizeUnit.PETABYTES::toBytes), 1000L * 1000 * 1000 * 1000 * 1000},
            {SizeUnit.PETABYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.PETABYTES::toKilobytes), 1000L * 1000 * 1000 * 1000},
            {SizeUnit.PETABYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.PETABYTES::toMegabytes), 1000L * 1000 * 1000},
            {SizeUnit.PETABYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.PETABYTES::toGigabytes), 1000L * 1000},
            {SizeUnit.PETABYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.PETABYTES::toTerabytes), 1000L},
            {SizeUnit.PETABYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.PETABYTES::toPetabytes), 1L},

            {SizeUnit.PETABYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.PETABYTES::toKibibytes), 1000L * 1000 * 1000 * 1000 * 1000 / 1024},
            {SizeUnit.PETABYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.PETABYTES::toMebibytes), 1000L * 1000 * 1000 * 1000 * 1000 / (1024L * 1024)},
            {SizeUnit.PETABYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.PETABYTES::toGibibytes), 1000L * 1000 * 1000 * 1000 * 1000 / (1024L * 1024 * 1024)},
            {SizeUnit.PETABYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.PETABYTES::toTebibytes), 1000L * 1000 * 1000 * 1000 * 1000 / (1024L * 1024 * 1024 * 1024)},
            {SizeUnit.PETABYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.PETABYTES::toPebibytes), 0L},

            // kibibytes
            {SizeUnit.KIBIBYTES, SizeUnit.BYTES, new Converter(SizeUnit.KIBIBYTES::toBytes), 1024L},
            {SizeUnit.KIBIBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.KIBIBYTES::toKilobytes), 1L},
            {SizeUnit.KIBIBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.KIBIBYTES::toMegabytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.KIBIBYTES::toGigabytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.KIBIBYTES::toTerabytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.KIBIBYTES::toPetabytes), 0L},

            {SizeUnit.KIBIBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.KIBIBYTES::toKibibytes), 1L},
            {SizeUnit.KIBIBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.KIBIBYTES::toMebibytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.KIBIBYTES::toGibibytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.KIBIBYTES::toTebibytes), 0L},
            {SizeUnit.KIBIBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.KIBIBYTES::toPebibytes), 0L},

            // mebibytes
            {SizeUnit.MEBIBYTES, SizeUnit.BYTES, new Converter(SizeUnit.MEBIBYTES::toBytes), 1024L * 1024},
            {SizeUnit.MEBIBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.MEBIBYTES::toKilobytes), 1024L * 1024 / 1000},
            {SizeUnit.MEBIBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.MEBIBYTES::toMegabytes), 1L},
            {SizeUnit.MEBIBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.MEBIBYTES::toGigabytes), 0L},
            {SizeUnit.MEBIBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.MEBIBYTES::toTerabytes), 0L},
            {SizeUnit.MEBIBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.MEBIBYTES::toPetabytes), 0L},

            {SizeUnit.MEBIBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.MEBIBYTES::toKibibytes), 1024L},
            {SizeUnit.MEBIBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.MEBIBYTES::toMebibytes), 1L},
            {SizeUnit.MEBIBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.MEBIBYTES::toGibibytes), 0L},
            {SizeUnit.MEBIBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.MEBIBYTES::toTebibytes), 0L},
            {SizeUnit.MEBIBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.MEBIBYTES::toPebibytes), 0L},

            // gibibytes
            {SizeUnit.GIBIBYTES, SizeUnit.BYTES, new Converter(SizeUnit.GIBIBYTES::toBytes), 1024L * 1024 * 1024},
            {SizeUnit.GIBIBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.GIBIBYTES::toKilobytes), 1024L * 1024 * 1024 / 1000},
            {SizeUnit.GIBIBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.GIBIBYTES::toMegabytes), 1024L * 1024 * 1024 / (1000L * 1000)},
            {SizeUnit.GIBIBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.GIBIBYTES::toGigabytes), 1L},
            {SizeUnit.GIBIBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.GIBIBYTES::toTerabytes), 0L},
            {SizeUnit.GIBIBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.GIBIBYTES::toPetabytes), 0L},

            {SizeUnit.GIBIBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.GIBIBYTES::toKibibytes), 1024L * 1024},
            {SizeUnit.GIBIBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.GIBIBYTES::toMebibytes), 1024L},
            {SizeUnit.GIBIBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.GIBIBYTES::toGibibytes), 1L},
            {SizeUnit.GIBIBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.GIBIBYTES::toTebibytes), 0L},
            {SizeUnit.GIBIBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.GIBIBYTES::toPebibytes), 0L},

            // tebibytes
            {SizeUnit.TEBIBYTES, SizeUnit.BYTES, new Converter(SizeUnit.TEBIBYTES::toBytes), 1024L * 1024 * 1024 * 1024},
            {SizeUnit.TEBIBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.TEBIBYTES::toKilobytes), 1024L * 1024 * 1024 * 1024 / 1000},
            {SizeUnit.TEBIBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.TEBIBYTES::toMegabytes), 1024L * 1024 * 1024 * 1024 / (1000L * 1000)},
            {SizeUnit.TEBIBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.TEBIBYTES::toGigabytes), 1024L * 1024 * 1024 * 1024 / (1000L * 1000 * 1000)},
            {SizeUnit.TEBIBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.TEBIBYTES::toTerabytes), 1L},
            {SizeUnit.TEBIBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.TEBIBYTES::toPetabytes), 0L},

            {SizeUnit.TEBIBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.TEBIBYTES::toKibibytes), 1024L * 1024 * 1024},
            {SizeUnit.TEBIBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.TEBIBYTES::toMebibytes), 1024L * 1024},
            {SizeUnit.TEBIBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.TEBIBYTES::toGibibytes), 1024L},
            {SizeUnit.TEBIBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.TEBIBYTES::toTebibytes), 1L},
            {SizeUnit.TEBIBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.TEBIBYTES::toPebibytes), 0L},

            // pebibytes
            {SizeUnit.PEBIBYTES, SizeUnit.BYTES, new Converter(SizeUnit.PEBIBYTES::toBytes), 1024L * 1024 * 1024 * 1024 * 1024},
            {SizeUnit.PEBIBYTES, SizeUnit.KILOBYTES, new Converter(SizeUnit.PEBIBYTES::toKilobytes), 1024L * 1024 * 1024 * 1024 * 1024 / 1000},
            {SizeUnit.PEBIBYTES, SizeUnit.MEGABYTES, new Converter(SizeUnit.PEBIBYTES::toMegabytes), 1024L * 1024 * 1024 * 1024 * 1024 / (1000L * 1000)},
            {SizeUnit.PEBIBYTES, SizeUnit.GIGABYTES, new Converter(SizeUnit.PEBIBYTES::toGigabytes), 1024L * 1024 * 1024 * 1024 * 1024 / (1000L * 1000 * 1000)},
            {SizeUnit.PEBIBYTES, SizeUnit.TERABYTES, new Converter(SizeUnit.PEBIBYTES::toTerabytes), 1024L * 1024 * 1024 * 1024 * 1024 / (1000L * 1000 * 1000 * 1000)},
            {SizeUnit.PEBIBYTES, SizeUnit.PETABYTES, new Converter(SizeUnit.PEBIBYTES::toPetabytes), 1L},

            {SizeUnit.PEBIBYTES, SizeUnit.KIBIBYTES, new Converter(SizeUnit.PEBIBYTES::toKibibytes), 1024L * 1024 * 1024 * 1024},
            {SizeUnit.PEBIBYTES, SizeUnit.MEBIBYTES, new Converter(SizeUnit.PEBIBYTES::toMebibytes), 1024L * 1024 * 1024},
            {SizeUnit.PEBIBYTES, SizeUnit.GIBIBYTES, new Converter(SizeUnit.PEBIBYTES::toGibibytes), 1024L * 1024},
            {SizeUnit.PEBIBYTES, SizeUnit.TEBIBYTES, new Converter(SizeUnit.PEBIBYTES::toTebibytes), 1024L},
            {SizeUnit.PEBIBYTES, SizeUnit.PEBIBYTES, new Converter(SizeUnit.PEBIBYTES::toPebibytes), 1L},
        });
    }

    @Test
    public void oneSrcUnitInDstUnits() throws Exception{
        assertThat(dst.convert(1, src))
            .isEqualTo(convertedValue);
        assertThat(srcConverter.toOneDst())
            .isEqualTo(convertedValue);
    }
}
