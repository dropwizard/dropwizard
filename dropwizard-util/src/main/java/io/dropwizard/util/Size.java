package io.dropwizard.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableSortedMap;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class Size implements Comparable<Size> {
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");

    private static final Map<String, SizeUnit> SUFFIXES = ImmutableSortedMap.<String, SizeUnit>orderedBy(String.CASE_INSENSITIVE_ORDER)
            .put("B", SizeUnit.BYTES)
            .put("byte", SizeUnit.BYTES)
            .put("bytes", SizeUnit.BYTES)
            .put("K", SizeUnit.KILOBYTES)
            .put("KB", SizeUnit.KILOBYTES)
            .put("KiB", SizeUnit.KIBIBYTES)
            .put("kilobyte", SizeUnit.KILOBYTES)
            .put("kibibyte", SizeUnit.KIBIBYTES)
            .put("kilobytes", SizeUnit.KILOBYTES)
            .put("kibibytes", SizeUnit.KIBIBYTES)
            .put("M", SizeUnit.MEGABYTES)
            .put("MB", SizeUnit.MEGABYTES)
            .put("MiB", SizeUnit.MEBIBYTES)
            .put("megabyte", SizeUnit.MEGABYTES)
            .put("mebibyte", SizeUnit.MEBIBYTES)
            .put("megabytes", SizeUnit.MEGABYTES)
            .put("mebibytes", SizeUnit.MEBIBYTES)
            .put("G", SizeUnit.GIGABYTES)
            .put("GB", SizeUnit.GIGABYTES)
            .put("GiB", SizeUnit.GIBIBYTES)
            .put("gigabyte", SizeUnit.GIGABYTES)
            .put("gibibyte", SizeUnit.GIBIBYTES)
            .put("gigabytes", SizeUnit.GIGABYTES)
            .put("gibibytes", SizeUnit.GIBIBYTES)
            .put("T", SizeUnit.TERABYTES)
            .put("TB", SizeUnit.TERABYTES)
            .put("TiB", SizeUnit.TEBIBYTES)
            .put("terabyte", SizeUnit.TERABYTES)
            .put("tebibyte", SizeUnit.TEBIBYTES)
            .put("terabytes", SizeUnit.TERABYTES)
            .put("tebibytes", SizeUnit.TEBIBYTES)
            .put("P", SizeUnit.PETABYTES)
            .put("PB", SizeUnit.PETABYTES)
            .put("PiB", SizeUnit.PEBIBYTES)
            .put("petabyte", SizeUnit.PETABYTES)
            .put("pebibyte", SizeUnit.PEBIBYTES)
            .put("petabytes", SizeUnit.PETABYTES)
            .put("pebibytes", SizeUnit.PEBIBYTES)
            .build();

    public static Size bytes(long count) {
        return new Size(count, SizeUnit.BYTES);
    }

    public static Size kilobytes(long count) {
        return new Size(count, SizeUnit.KILOBYTES);
    }

    public static Size megabytes(long count) {
        return new Size(count, SizeUnit.MEGABYTES);
    }

    public static Size gigabytes(long count) {
        return new Size(count, SizeUnit.GIGABYTES);
    }

    public static Size terabytes(long count) {
        return new Size(count, SizeUnit.TERABYTES);
    }

    public static Size petabytes(long count) {
        return new Size(count, SizeUnit.PETABYTES);
    }

    public static Size kibibytes(long count) {
        return new Size(count, SizeUnit.KIBIBYTES);
    }

    public static Size mebibytes(long count) {
        return new Size(count, SizeUnit.MEBIBYTES);
    }

    public static Size gibibytes(long count) {
        return new Size(count, SizeUnit.GIBIBYTES);
    }

    public static Size tebibytes(long count) {
        return new Size(count, SizeUnit.TEBIBYTES);
    }

    public static Size pebibytes(long count) {
        return new Size(count, SizeUnit.PEBIBYTES);
    }

    @JsonCreator
    public static Size parse(String size) {
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        checkArgument(matcher.matches(), "Invalid size: " + size);

        final long count = Long.parseLong(matcher.group(1));
        final SizeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new Size(count, unit);
    }

    private final long count;
    private final SizeUnit unit;

    private Size(long count, SizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    public long getQuantity() {
        return count;
    }

    public SizeUnit getUnit() {
        return unit;
    }

    public long toBytes() {
        return SizeUnit.BYTES.convert(count, unit);
    }

    public long toKilobytes() {
        return SizeUnit.KILOBYTES.convert(count, unit);
    }

    public long toMegabytes() {
        return SizeUnit.MEGABYTES.convert(count, unit);
    }

    public long toGigabytes() {
        return SizeUnit.GIGABYTES.convert(count, unit);
    }

    public long toTerabytes() {
        return SizeUnit.TERABYTES.convert(count, unit);
    }

    public long toPetabytes() {
        return SizeUnit.PETABYTES.convert(count, unit);
    }

    public long toKibibytes() {
        return SizeUnit.KIBIBYTES.convert(count, unit);
    }

    public long toMebibytes() {
        return SizeUnit.MEBIBYTES.convert(count, unit);
    }

    public long toGibibytes() {
        return SizeUnit.GIBIBYTES.convert(count, unit);
    }

    public long toTebibytes() {
        return SizeUnit.TEBIBYTES.convert(count, unit);
    }

    public long toPebibytes() {
        return SizeUnit.PEBIBYTES.convert(count, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final Size size = (Size) obj;
        return (count == size.count) && (unit == size.unit);
    }

    @Override
    public int hashCode() {
        return (31 * (int) (count ^ (count >>> 32))) + unit.hashCode();
    }

    @Override
    @JsonValue
    public String toString() {
        String units = unit.toString().toLowerCase(Locale.ENGLISH);
        if (count == 1) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    @Override
    public int compareTo(Size other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toBytes(), other.toBytes());
    }
}
