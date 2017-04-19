package io.dropwizard.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import org.joda.time.DateTime;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class DateTimeOffset {
    private static final Pattern PATTERN = Pattern.compile("([-+]?[\\d]+)[\\s]*(" +
        "ms|msec(s)?|millisecond(s)?|" +
        "s|sec(s)?|second(s)?|" +
        "m|min(s)?|minute(s)?|" +
        "h||hr(s)?|hour(s)?|" +
        "d|day(s)?|" +
        "w|wk(s)?|week(s)?|" +
        "mo(s)?|month(s)?|" +
        "y|yr(s)?|year(s)?" +
        ')');
    private static final ImmutableMap<String, OffsetUnit> SUFFIXES;

    static {
        final ImmutableMap.Builder<String, OffsetUnit> suffixes = ImmutableMap.builder();

        suffixes.put("ms", OffsetUnit.MILLISECONDS);
        suffixes.put("msec", OffsetUnit.MILLISECONDS);
        suffixes.put("msecs", OffsetUnit.MILLISECONDS);
        suffixes.put("millisecond", OffsetUnit.MILLISECONDS);
        suffixes.put("milliseconds", OffsetUnit.MILLISECONDS);

        suffixes.put("s", OffsetUnit.SECONDS);
        suffixes.put("sec", OffsetUnit.SECONDS);
        suffixes.put("secs", OffsetUnit.SECONDS);
        suffixes.put("second", OffsetUnit.SECONDS);
        suffixes.put("seconds", OffsetUnit.SECONDS);

        suffixes.put("m", OffsetUnit.MINUTES);
        suffixes.put("min", OffsetUnit.MINUTES);
        suffixes.put("mins", OffsetUnit.MINUTES);
        suffixes.put("minute", OffsetUnit.MINUTES);
        suffixes.put("minutes", OffsetUnit.MINUTES);

        suffixes.put("h", OffsetUnit.HOURS);
        suffixes.put("hr", OffsetUnit.HOURS);
        suffixes.put("hrs", OffsetUnit.HOURS);
        suffixes.put("hour", OffsetUnit.HOURS);
        suffixes.put("hours", OffsetUnit.HOURS);

        suffixes.put("d", OffsetUnit.DAYS);
        suffixes.put("day", OffsetUnit.DAYS);
        suffixes.put("days", OffsetUnit.DAYS);

        suffixes.put("w", OffsetUnit.WEEKS);
        suffixes.put("wk", OffsetUnit.WEEKS);
        suffixes.put("wks", OffsetUnit.WEEKS);
        suffixes.put("week", OffsetUnit.WEEKS);
        suffixes.put("weeks", OffsetUnit.WEEKS);

        suffixes.put("mo", OffsetUnit.MONTHS);
        suffixes.put("mos", OffsetUnit.MONTHS);
        suffixes.put("month", OffsetUnit.MONTHS);
        suffixes.put("months", OffsetUnit.MONTHS);

        suffixes.put("y", OffsetUnit.YEARS);
        suffixes.put("yr", OffsetUnit.YEARS);
        suffixes.put("yrs", OffsetUnit.YEARS);
        suffixes.put("year", OffsetUnit.YEARS);
        suffixes.put("years", OffsetUnit.YEARS);

        SUFFIXES = suffixes.build();
    }

    public static DateTimeOffset milliseconds(int count) {
        return new DateTimeOffset(count, OffsetUnit.MILLISECONDS);
    }

    public static DateTimeOffset seconds(int count) {
        return new DateTimeOffset(count, OffsetUnit.SECONDS);
    }

    public static DateTimeOffset minutes(int count) {
        return new DateTimeOffset(count, OffsetUnit.MINUTES);
    }

    public static DateTimeOffset hours(int count) {
        return new DateTimeOffset(count, OffsetUnit.HOURS);
    }

    public static DateTimeOffset days(int count) {
        return new DateTimeOffset(count, OffsetUnit.DAYS);
    }

    public static DateTimeOffset weeks(int count) {
        return new DateTimeOffset(count, OffsetUnit.WEEKS);
    }

    public static DateTimeOffset months(int count) {
        return new DateTimeOffset(count, OffsetUnit.MONTHS);
    }

    public static DateTimeOffset years(int count) {
        return new DateTimeOffset(count, OffsetUnit.YEARS);
    }

    private static int parseCount(Matcher matcher) {
        return Integer.parseInt(matcher.group(1));
    }

    private static OffsetUnit parseUnit(Matcher matcher) {
        return SUFFIXES.get(matcher.group(2));
    }

    @JsonCreator
    public static DateTimeOffset parse(String offset) {
        Matcher matcher = PATTERN.matcher(offset);
        checkArgument(matcher.matches(), "Invalid date-time offset: %s", offset);
        return new DateTimeOffset(parseCount(matcher), parseUnit(matcher));
    }

    private final int count;
    private final OffsetUnit unit;

    private DateTimeOffset(int count, OffsetUnit unit) {
        this.count = count;
        this.unit = checkNotNull(unit);
    }

    public long getQuantity() {
        return count;
    }

    public OffsetUnit getUnit() {
        return unit;
    }

    public DateTime apply(DateTime dateTime) {
        switch (unit) {
            case MILLISECONDS:
                return dateTime.plusMillis(count);

            case SECONDS:
                return dateTime.plusSeconds(count);

            case MINUTES:
                return dateTime.plusMinutes(count);

            case HOURS:
                return dateTime.plusHours(count);

            case DAYS:
                return dateTime.plusDays(count);

            case WEEKS:
                return dateTime.plusWeeks(count);

            case MONTHS:
                return dateTime.plusMonths(count);

            case YEARS:
                return dateTime.plusYears(count);
        }

        throw new UnsupportedOperationException("invalid unit value");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if ((obj == null) || (getClass() != obj.getClass())) { return false; }
        final DateTimeOffset duration = (DateTimeOffset) obj;
        return (count == duration.count) && (unit == duration.unit);

    }

    @Override
    public int hashCode() {
        int result = count;
        result = 31 * result + (unit != null ? unit.hashCode() : 0);
        return result;
    }

    @Override
    @JsonValue
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1 || count == -1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }
}
