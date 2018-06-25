package io.dropwizard.logging.json.layout;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A faster timestamp formatter than the default one in Logback.
 * Also produces timestamps as numbers if the timestamp formatting is disabled.
 */
public class TimestampFormatter {
    private static final Map<String, DateTimeFormatter> FORMATTERS;

    static {
        final Map<String, DateTimeFormatter> formatters = new HashMap<>();
        formatters.put("ISO_LOCAL_DATE", DateTimeFormatter.ISO_LOCAL_DATE);
        formatters.put("ISO_OFFSET_DATE", DateTimeFormatter.ISO_OFFSET_DATE);
        formatters.put("ISO_DATE", DateTimeFormatter.ISO_DATE);
        formatters.put("ISO_LOCAL_TIME", DateTimeFormatter.ISO_LOCAL_TIME);
        formatters.put("ISO_OFFSET_TIME", DateTimeFormatter.ISO_OFFSET_TIME);
        formatters.put("ISO_TIME", DateTimeFormatter.ISO_TIME);
        formatters.put("ISO_LOCAL_DATE_TIME", DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        formatters.put("ISO_OFFSET_DATE_TIME", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        formatters.put("ISO_ZONED_DATE_TIME", DateTimeFormatter.ISO_ZONED_DATE_TIME);
        formatters.put("ISO_DATE_TIME", DateTimeFormatter.ISO_DATE_TIME);
        formatters.put("ISO_ORDINAL_DATE", DateTimeFormatter.ISO_ORDINAL_DATE);
        formatters.put("ISO_WEEK_DATE", DateTimeFormatter.ISO_WEEK_DATE);
        formatters.put("ISO_INSTANT", DateTimeFormatter.ISO_INSTANT);
        formatters.put("BASIC_ISO_DATE", DateTimeFormatter.BASIC_ISO_DATE);
        formatters.put("RFC_1123_DATE_TIME", DateTimeFormatter.RFC_1123_DATE_TIME);
        FORMATTERS = Collections.unmodifiableMap(formatters);
    }

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
