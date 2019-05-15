package io.dropwizard.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public class EnumsTest {

    enum VideoFormat {
        OGG,
        MPEG_DASH,
        H_264,
        FFMPEG,
        HDMOV {
            @Override
            public String toString() {
                return "QuickTime";
            }
        };

        // Factory methods are not handled
        public static VideoFormat fromString(String s) {
            return valueOf(s.replace("[", "").replace("]", ""));
        }
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("OGG", VideoFormat.OGG),
                Arguments.of("ogg", VideoFormat.OGG),
                Arguments.of("FFmpeg", VideoFormat.FFMPEG),
                Arguments.of(" FFmpeg ", VideoFormat.FFMPEG),
                Arguments.of("MPEG-DASH", VideoFormat.MPEG_DASH),
                Arguments.of("h.264", VideoFormat.H_264),
                Arguments.of("QuickTime", VideoFormat.HDMOV),
                Arguments.of("[OGG]", null),
                Arguments.of("FLV", null)
        );
    }


    @ParameterizedTest
    @MethodSource("data")
    public void canGuess(String text, VideoFormat result) {
        assertThat(Enums.fromStringFuzzy(text, VideoFormat.values())).isEqualTo(result);
    }
}
