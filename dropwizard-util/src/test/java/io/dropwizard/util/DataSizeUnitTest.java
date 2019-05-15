package io.dropwizard.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class DataSizeUnitTest {
    private static class Converter {
        private Function<Long, Long> f;

        Converter(Function<Long, Long> f) {
            this.f = f;
        }

        long toOneDst() {
            return f.apply(1L);
        }
    }

    static Stream<Arguments> parameters() {
        return Stream.of(
                // bytes
                arguments(DataSizeUnit.BYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.BYTES::toBytes), 1L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.BYTES::toKilobytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.BYTES::toMegabytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.BYTES::toGigabytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.BYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.BYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.BYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.BYTES::toKibibytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.BYTES::toMebibytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.BYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.BYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.BYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.BYTES::toPebibytes), 0L),

                // kilobytes
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.KILOBYTES::toBytes), 1000L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.KILOBYTES::toKilobytes), 1L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.KILOBYTES::toMegabytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.KILOBYTES::toGigabytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.KILOBYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.KILOBYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.KILOBYTES::toKibibytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.KILOBYTES::toMebibytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.KILOBYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.KILOBYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.KILOBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.KILOBYTES::toPebibytes), 0L),

                // megabytes
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.MEGABYTES::toBytes), 1000L * 1000L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.MEGABYTES::toKilobytes), 1000L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.MEGABYTES::toMegabytes), 1L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.MEGABYTES::toGigabytes), 0L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.MEGABYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.MEGABYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.MEGABYTES::toKibibytes), 1000L * 1000L / 1024L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.MEGABYTES::toMebibytes), 0L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.MEGABYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.MEGABYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.MEGABYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.MEGABYTES::toPebibytes), 0L),

                // gigabytes
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.GIGABYTES::toBytes), 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.GIGABYTES::toKilobytes), 1000L * 1000L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.GIGABYTES::toMegabytes), 1000L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.GIGABYTES::toGigabytes), 1L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.GIGABYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.GIGABYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.GIGABYTES::toKibibytes), 1000L * 1000L * 1000L / 1024L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.GIGABYTES::toMebibytes), 1000L * 1000L * 1000L / (1024L * 1024L)),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.GIGABYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.GIGABYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.GIGABYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.GIGABYTES::toPebibytes), 0L),

                // terabytes
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.TERABYTES::toBytes), 1000L * 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.TERABYTES::toKilobytes), 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.TERABYTES::toMegabytes), 1000L * 1000L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.TERABYTES::toGigabytes), 1000L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.TERABYTES::toTerabytes), 1L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.TERABYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.TERABYTES::toKibibytes), 1000L * 1000L * 1000L * 1000L / 1024L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.TERABYTES::toMebibytes), 1000L * 1000L * 1000L * 1000L / (1024L * 1024L)),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.TERABYTES::toGibibytes), 1000L * 1000L * 1000L * 1000L / (1024L * 1024L * 1024L)),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.TERABYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.TERABYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.TERABYTES::toPebibytes), 0L),

                // petabytes
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.PETABYTES::toBytes), 1000L * 1000L * 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.PETABYTES::toKilobytes), 1000L * 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.PETABYTES::toMegabytes), 1000L * 1000L * 1000L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.PETABYTES::toGigabytes), 1000L * 1000L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.PETABYTES::toTerabytes), 1000L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.PETABYTES::toPetabytes), 1L),

                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.PETABYTES::toKibibytes), 1000L * 1000L * 1000L * 1000L * 1000L / 1024L),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.PETABYTES::toMebibytes), 1000L * 1000L * 1000L * 1000L * 1000L / (1024L * 1024L)),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.PETABYTES::toGibibytes), 1000L * 1000L * 1000L * 1000L * 1000L / (1024L * 1024L * 1024L)),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.PETABYTES::toTebibytes), 1000L * 1000L * 1000L * 1000L * 1000L / (1024L * 1024L * 1024L * 1024L)),
                arguments(DataSizeUnit.PETABYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.PETABYTES::toPebibytes), 0L),

                // kibibytes
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.KIBIBYTES::toBytes), 1024L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.KIBIBYTES::toKilobytes), 1L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.KIBIBYTES::toMegabytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.KIBIBYTES::toGigabytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.KIBIBYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.KIBIBYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.KIBIBYTES::toKibibytes), 1L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.KIBIBYTES::toMebibytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.KIBIBYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.KIBIBYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.KIBIBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.KIBIBYTES::toPebibytes), 0L),

                // mebibytes
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.MEBIBYTES::toBytes), 1024L * 1024L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.MEBIBYTES::toKilobytes), 1024L * 1024L / 1000L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.MEBIBYTES::toMegabytes), 1L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.MEBIBYTES::toGigabytes), 0L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.MEBIBYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.MEBIBYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.MEBIBYTES::toKibibytes), 1024L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.MEBIBYTES::toMebibytes), 1L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.MEBIBYTES::toGibibytes), 0L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.MEBIBYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.MEBIBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.MEBIBYTES::toPebibytes), 0L),

                // gibibytes
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.GIBIBYTES::toBytes), 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.GIBIBYTES::toKilobytes), 1024L * 1024L * 1024L / 1000L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.GIBIBYTES::toMegabytes), 1024L * 1024L * 1024L / (1000L * 1000L)),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.GIBIBYTES::toGigabytes), 1L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.GIBIBYTES::toTerabytes), 0L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.GIBIBYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.GIBIBYTES::toKibibytes), 1024L * 1024L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.GIBIBYTES::toMebibytes), 1024L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.GIBIBYTES::toGibibytes), 1L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.GIBIBYTES::toTebibytes), 0L),
                arguments(DataSizeUnit.GIBIBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.GIBIBYTES::toPebibytes), 0L),

                // tebibytes
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.TEBIBYTES::toBytes), 1024L * 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.TEBIBYTES::toKilobytes), 1024L * 1024L * 1024L * 1024L / 1000L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.TEBIBYTES::toMegabytes), 1024L * 1024L * 1024L * 1024L / (1000L * 1000L)),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.TEBIBYTES::toGigabytes), 1024L * 1024L * 1024L * 1024L / (1000L * 1000L * 1000L)),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.TEBIBYTES::toTerabytes), 1L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.TEBIBYTES::toPetabytes), 0L),

                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.TEBIBYTES::toKibibytes), 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.TEBIBYTES::toMebibytes), 1024L * 1024L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.TEBIBYTES::toGibibytes), 1024L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.TEBIBYTES::toTebibytes), 1L),
                arguments(DataSizeUnit.TEBIBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.TEBIBYTES::toPebibytes), 0L),

                // pebibytes
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.BYTES, new Converter(DataSizeUnit.PEBIBYTES::toBytes), 1024L * 1024L * 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.KILOBYTES, new Converter(DataSizeUnit.PEBIBYTES::toKilobytes), 1024L * 1024L * 1024L * 1024L * 1024L / 1000L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.MEGABYTES, new Converter(DataSizeUnit.PEBIBYTES::toMegabytes), 1024L * 1024L * 1024L * 1024L * 1024L / (1000L * 1000L)),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.GIGABYTES, new Converter(DataSizeUnit.PEBIBYTES::toGigabytes), 1024L * 1024L * 1024L * 1024L * 1024L / (1000L * 1000L * 1000L)),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.TERABYTES, new Converter(DataSizeUnit.PEBIBYTES::toTerabytes), 1024L * 1024L * 1024L * 1024L * 1024L / (1000L * 1000L * 1000L * 1000L)),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.PETABYTES, new Converter(DataSizeUnit.PEBIBYTES::toPetabytes), 1L),

                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.KIBIBYTES, new Converter(DataSizeUnit.PEBIBYTES::toKibibytes), 1024L * 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.MEBIBYTES, new Converter(DataSizeUnit.PEBIBYTES::toMebibytes), 1024L * 1024L * 1024L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.GIBIBYTES, new Converter(DataSizeUnit.PEBIBYTES::toGibibytes), 1024L * 1024L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.TEBIBYTES, new Converter(DataSizeUnit.PEBIBYTES::toTebibytes), 1024L),
                arguments(DataSizeUnit.PEBIBYTES, DataSizeUnit.PEBIBYTES, new Converter(DataSizeUnit.PEBIBYTES::toPebibytes), 1L));
    }

    @ParameterizedTest(name = "{0} => {1}")
    @MethodSource("parameters")
    void oneSrcUnitInDstUnits(DataSizeUnit src, DataSizeUnit dst, Converter c, long value) {
        assertThat(dst.convert(1, src)).isEqualTo(value);
        assertThat(c.toOneDst()).isEqualTo(value);
    }
}