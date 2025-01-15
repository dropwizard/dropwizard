package io.dropwizard.logging.json.layout;

import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

/**
 * A faster timestamp formatter than the default one in Logback.
 * Also produces timestamps as numbers if the timestamp formatting is disabled.
 */
public class TimestampFormatter {
    private static final Map<String, DateTimeFormatter> FORMATTERS = Map.ofEntries(
        entry("ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE),
        entry("ISO_OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE),
        entry("ISO_DATE", DateTimeFormatter.ISO_DATE),
        entry("ISO_LOCAL_TIME", DateTimeFormatter.ISO_LOCAL_TIME),
        entry("ISO_OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME),
        entry("ISO_TIME", DateTimeFormatter.ISO_TIME),
        entry("ISO_LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME),
        entry("ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME),
        entry("ISO_ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME),
        entry("ISO_DATE_TIME", DateTimeFormatter.ISO_DATE_TIME),
        entry("ISO_ORDINAL_DATE", DateTimeFormatter.ISO_ORDINAL_DATE),
        entry("ISO_WEEK_DATE", DateTimeFormatter.ISO_WEEK_DATE),
        entry("ISO_INSTANT", DateTimeFormatter.ISO_INSTANT),
        entry("BASIC_ISO_DATE", DateTimeFormatter.BASIC_ISO_DATE),
        entry("RFC_1123_DATE_TIME", DateTimeFormatter.RFC_1123_DATE_TIME));

    @Nullable
    private final DateTimeFormatter dateTimeFormatter;

    public TimestampFormatter(@Nullable String timestampFormat, ZoneId zoneId) {
        if (timestampFormat != null) {
            dateTimeFormatter = Optional.ofNullable(FORMATTERS.get(timestampFormat))
                .orElseGet(() -> DateTimeFormatter.ofPattern(timestampFormat))
                .withZone(zoneId);
        } else {
            dateTimeFormatter = null;
        }
    }

    public Object format(long timestamp) {
        return dateTimeFormatter == null ? timestamp : dateTimeFormatter.format(Instant.ofEpochMilli(timestamp));
    }
}
