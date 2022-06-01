package io.dropwizard.logging.json.layout;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class TimestampFormatterTest {

    private final long timestamp = 1513956631000L;

    @Test
    void testFormatTimestampAsString() {
        TimestampFormatter timestampFormatter =
                new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ZoneId.of("GMT+01:00"));
        assertThat(timestampFormatter.format(timestamp)).isEqualTo("2017-12-22T16:30:31.000+0100");
    }

    @Test
    void testFormatTimestampFromPredefinedFormat() {
        TimestampFormatter timestampFormatter = new TimestampFormatter("RFC_1123_DATE_TIME", ZoneId.of("GMT+01:00"));
        assertThat(timestampFormatter.format(timestamp)).isEqualTo("Fri, 22 Dec 2017 16:30:31 +0100");
    }

    @Test
    void testDoNotFormatTimestamp() {
        TimestampFormatter timestampFormatter = new TimestampFormatter(null, ZoneId.of("GMT+01:00"));
        assertThat(timestampFormatter.format(timestamp)).isEqualTo(timestamp);
    }
}
