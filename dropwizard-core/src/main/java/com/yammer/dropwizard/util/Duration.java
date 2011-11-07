package com.yammer.dropwizard.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;

import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkNotNull;

public class Duration {
    private static final ImmutableMap<String, TimeUnit> SUFFIXES;
    static {
        final ImmutableMap.Builder<String, TimeUnit> suffixes = ImmutableMap.builder();

        suffixes.put("ns", TimeUnit.NANOSECONDS);
        suffixes.put("nanosecond", TimeUnit.NANOSECONDS);
        suffixes.put("nanoseconds", TimeUnit.NANOSECONDS);

        suffixes.put("us", TimeUnit.MICROSECONDS);
        suffixes.put("microsecond", TimeUnit.MICROSECONDS);
        suffixes.put("microseconds", TimeUnit.MICROSECONDS);

        suffixes.put("ms", TimeUnit.MILLISECONDS);
        suffixes.put("millisecond", TimeUnit.MILLISECONDS);
        suffixes.put("milliseconds", TimeUnit.MILLISECONDS);

        suffixes.put("s", TimeUnit.SECONDS);
        suffixes.put("second", TimeUnit.SECONDS);
        suffixes.put("seconds", TimeUnit.SECONDS);

        suffixes.put("m", TimeUnit.MINUTES);
        suffixes.put("minute", TimeUnit.MINUTES);
        suffixes.put("minutes", TimeUnit.MINUTES);

        suffixes.put("h", TimeUnit.HOURS);
        suffixes.put("hour", TimeUnit.HOURS);
        suffixes.put("hours", TimeUnit.HOURS);

        suffixes.put("d", TimeUnit.DAYS);
        suffixes.put("day", TimeUnit.DAYS);
        suffixes.put("days", TimeUnit.DAYS);

        SUFFIXES = suffixes.build();
    }
    
    public static Duration nanoseconds(long count) {
        return new Duration(count, TimeUnit.NANOSECONDS);
    }

    public static Duration microseconds(long count) {
        return new Duration(count, TimeUnit.MICROSECONDS);
    }

    public static Duration milliseconds(long count) {
        return new Duration(count, TimeUnit.MILLISECONDS);
    }

    public static Duration seconds(long count) {
        return new Duration(count, TimeUnit.SECONDS);
    }

    public static Duration minutes(long count) {
        return new Duration(count, TimeUnit.MINUTES);
    }

    public static Duration hours(long count) {
        return new Duration(count, TimeUnit.HOURS);
    }

    public static Duration days(long count) {
        return new Duration(count, TimeUnit.DAYS);
    }
    
    private static long parseCount(String s) {
        final String value = CharMatcher.WHITESPACE.removeFrom(s);
        return Long.parseLong(CharMatcher.JAVA_LETTER.trimTrailingFrom(value));
    }
    
    private static TimeUnit parseUnit(String s) {
        final String value = CharMatcher.WHITESPACE.removeFrom(s);
        final String suffix = CharMatcher.DIGIT.trimLeadingFrom(value);
        final TimeUnit unit = SUFFIXES.get(suffix);
        if (unit != null) {
            return unit;
        }
        throw new IllegalArgumentException("Unable to parse as duration: " + s);
    }

    private final long count;
    private final TimeUnit unit;
    
    public Duration(String s) {
        this(parseCount(s), parseUnit(s));
    }

    private Duration(long count, TimeUnit unit) {
        this.count = count;
        this.unit = checkNotNull(unit);
    }

    public long toNanoseconds() {
        return TimeUnit.NANOSECONDS.convert(count, unit);
    }

    public long toMicroseconds() {
        return TimeUnit.MICROSECONDS.convert(count, unit);
    }

    public long toMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(count, unit);
    }

    public long toSeconds() {
        return TimeUnit.SECONDS.convert(count, unit);
    }

    public long toMinutes() {
        return TimeUnit.MINUTES.convert(count, unit);
    }

    public long toHours() {
        return TimeUnit.HOURS.convert(count, unit);
    }

    public long toDays() {
        return TimeUnit.DAYS.convert(count, unit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Duration duration = (Duration) o;
        return count == duration.count && unit == duration.unit;

    }

    @Override
    public int hashCode() {
        return 31 * (int) (count ^ (count >>> 32)) + unit.hashCode();
    }

    @Override
    public String toString() {
        String units = unit.toString().toLowerCase();
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + " " + units;
    }
}
