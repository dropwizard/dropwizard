package io.dropwizard.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * This class provides helper methods for parsing human-readable duration values.
 */
public class Duration implements Comparable<Duration>, Serializable {
    private static final long serialVersionUID = 1445611723318059801L;

    private static final Pattern DURATION_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");
    private static final Map<String, TimeUnit> SUFFIXES;

    static {
        final Map<String, TimeUnit> suffixes = new HashMap<>();
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
        suffixes.put("min", TimeUnit.MINUTES);
        suffixes.put("mins", TimeUnit.MINUTES);
        suffixes.put("minute", TimeUnit.MINUTES);
        suffixes.put("minutes", TimeUnit.MINUTES);
        suffixes.put("h", TimeUnit.HOURS);
        suffixes.put("hour", TimeUnit.HOURS);
        suffixes.put("hours", TimeUnit.HOURS);
        suffixes.put("d", TimeUnit.DAYS);
        suffixes.put("day", TimeUnit.DAYS);
        suffixes.put("days", TimeUnit.DAYS);
        SUFFIXES = Collections.unmodifiableMap(suffixes);

    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of nanoseconds.
     *
     * @param count the amount of nanoseconds
     * @return the newly created {@link Duration} object
     */
    public static Duration nanoseconds(long count) {
        return new Duration(count, TimeUnit.NANOSECONDS);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of microseconds.
     *
     * @param count the amount of microseconds
     * @return the newly created {@link Duration} object
     */
    public static Duration microseconds(long count) {
        return new Duration(count, TimeUnit.MICROSECONDS);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of milliseconds.
     *
     * @param count the amount of milliseconds
     * @return the newly created {@link Duration} object
     */
    public static Duration milliseconds(long count) {
        return new Duration(count, TimeUnit.MILLISECONDS);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of seconds.
     *
     * @param count the amount of seconds
     * @return the newly created {@link Duration} object
     */
    public static Duration seconds(long count) {
        return new Duration(count, TimeUnit.SECONDS);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of minutes.
     *
     * @param count the amount of minutes
     * @return the newly created {@link Duration} object
     */
    public static Duration minutes(long count) {
        return new Duration(count, TimeUnit.MINUTES);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of hours.
     *
     * @param count the amount of hours
     * @return the newly created {@link Duration} object
     */
    public static Duration hours(long count) {
        return new Duration(count, TimeUnit.HOURS);
    }

    /**
     * Constructs a new {@link Duration} object representing the specified amount of days.
     *
     * @param count the amount of days
     * @return the newly created {@link Duration} object
     */
    public static Duration days(long count) {
        return new Duration(count, TimeUnit.DAYS);
    }

    /**
     * Parses a given input string to a {@link Duration}.
     *
     * @param duration the string to parse
     * @return a valid {@link Duration} representing the parsed input string
     * @throws IllegalArgumentException if the given input string cannot be parsed correctly
     */
    @JsonCreator
    public static Duration parse(String duration) {
        final Matcher matcher = DURATION_PATTERN.matcher(duration);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid duration: " + duration);
        }

        final long count = Long.parseLong(matcher.group(1));
        final TimeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid duration: " + duration + ". Wrong time unit");
        }

        return new Duration(count, unit);
    }

    /**
     * The quantity of the current duration
     */
    private final long count;

    /**
     * The time unit of the current duration
     */
    private final TimeUnit unit;

    private Duration(long count, TimeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    /**
     * Gets the quantity of the current {@link Duration} object.
     *
     * @return the quantity of the current duration
     */
    public long getQuantity() {
        return count;
    }

    /**
     * Returns the {@link TimeUnit time unit} of the current {@link Duration} object.
     *
     * @return the unit of the current duration
     */
    public TimeUnit getUnit() {
        return unit;
    }

    /**
     * Returns the quantity of the current {@link Duration} object in nanoseconds.
     *
     * @return the converted quantity
     */
    public long toNanoseconds() {
        return TimeUnit.NANOSECONDS.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in microseconds.
     *
     * @return the converted quantity
     */
    public long toMicroseconds() {
        return TimeUnit.MICROSECONDS.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in milliseconds.
     *
     * @return the converted quantity
     */
    public long toMilliseconds() {
        return TimeUnit.MILLISECONDS.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in seconds.
     *
     * @return the converted quantity
     */
    public long toSeconds() {
        return TimeUnit.SECONDS.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in minutes.
     *
     * @return the converted quantity
     */
    public long toMinutes() {
        return TimeUnit.MINUTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in hours.
     *
     * @return the converted quantity
     */
    public long toHours() {
        return TimeUnit.HOURS.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Duration} object in days.
     *
     * @return the converted quantity
     */
    public long toDays() {
        return TimeUnit.DAYS.convert(count, unit);
    }

    /**
     * Constructs a {@code java.time.Duration} from the current {@link Duration} object.
     *
     * @return the {@code java.time.Duration} representation
     */
    public java.time.Duration toJavaDuration() {
        return java.time.Duration.ofNanos(toNanoseconds());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final Duration duration = (Duration) obj;
        return (count == duration.count) && (unit == duration.unit);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (31 * (int) (count ^ (count >>> 32))) + unit.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @JsonValue
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Duration other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toNanoseconds(), other.toNanoseconds());
    }
}
