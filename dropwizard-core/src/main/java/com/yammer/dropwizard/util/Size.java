package com.yammer.dropwizard.util;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableMap;

import static com.google.common.base.Preconditions.checkNotNull;

public class Size {
    private static final ImmutableMap<String, SizeUnit> SUFFIXES;
    static {
        final ImmutableMap.Builder<String, SizeUnit> suffixes = ImmutableMap.builder();
        suffixes.put("B", SizeUnit.BYTES);
        suffixes.put("byte", SizeUnit.BYTES);
        suffixes.put("bytes", SizeUnit.BYTES);

        suffixes.put("KB", SizeUnit.KILOBYTES);
        suffixes.put("KiB", SizeUnit.KILOBYTES);
        suffixes.put("kilobyte", SizeUnit.KILOBYTES);
        suffixes.put("kilobytes", SizeUnit.KILOBYTES);

        suffixes.put("MB", SizeUnit.MEGABYTES);
        suffixes.put("MiB", SizeUnit.MEGABYTES);
        suffixes.put("megabyte", SizeUnit.MEGABYTES);
        suffixes.put("megabytes", SizeUnit.MEGABYTES);

        suffixes.put("GB", SizeUnit.GIGABYTES);
        suffixes.put("GiB", SizeUnit.GIGABYTES);
        suffixes.put("gigabyte", SizeUnit.GIGABYTES);
        suffixes.put("gigabytes", SizeUnit.GIGABYTES);

        suffixes.put("TB", SizeUnit.TERABYTES);
        suffixes.put("TiB", SizeUnit.TERABYTES);
        suffixes.put("terabyte", SizeUnit.TERABYTES);
        suffixes.put("terabytes", SizeUnit.TERABYTES);

        SUFFIXES = suffixes.build();
    }
    
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

    private static long parseCount(String s) {
        final String value = CharMatcher.WHITESPACE.removeFrom(s);
        return Long.parseLong(CharMatcher.JAVA_LETTER.trimTrailingFrom(value));
    }

    private static SizeUnit parseUnit(String s) {
        final String value = CharMatcher.WHITESPACE.removeFrom(s);
        final String suffix = CharMatcher.DIGIT.trimLeadingFrom(value).trim();
        final SizeUnit unit = SUFFIXES.get(suffix);
        if (unit != null) {
            return unit;
        }
        throw new IllegalArgumentException("Unable to parse as size: " + s);
    }

    private final long count;
    private final SizeUnit unit;
    
    public Size(String s) {
        this(parseCount(s), parseUnit(s));
    }

    private Size(long count, SizeUnit unit) {
        this.count = count;
        this.unit = checkNotNull(unit);
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

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }
        final Size size = (Size) o;
        return count == size.count && unit == size.unit;
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
