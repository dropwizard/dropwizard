package io.dropwizard.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * A data size with SI or IEC prefix, such as "128KB" or "5 Gibibytes".
 * This class models a size in terms of bytes and is immutable and thread-safe.
 *
 * @see DataSizeUnit
 * @since 2.0
 */
public class DataSize implements Comparable<DataSize> {
    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)\\s*(\\S*)");

    private static final SortedMap<String, DataSizeUnit> SUFFIXES;

    static {
        final SortedMap<String, DataSizeUnit> suffixes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        suffixes.put("B", DataSizeUnit.BYTES);
        suffixes.put("byte", DataSizeUnit.BYTES);
        suffixes.put("bytes", DataSizeUnit.BYTES);
        suffixes.put("K", DataSizeUnit.KILOBYTES);
        suffixes.put("KB", DataSizeUnit.KILOBYTES);
        suffixes.put("KiB", DataSizeUnit.KIBIBYTES);
        suffixes.put("kilobyte", DataSizeUnit.KILOBYTES);
        suffixes.put("kibibyte", DataSizeUnit.KIBIBYTES);
        suffixes.put("kilobytes", DataSizeUnit.KILOBYTES);
        suffixes.put("kibibytes", DataSizeUnit.KIBIBYTES);
        suffixes.put("M", DataSizeUnit.MEGABYTES);
        suffixes.put("MB", DataSizeUnit.MEGABYTES);
        suffixes.put("MiB", DataSizeUnit.MEBIBYTES);
        suffixes.put("megabyte", DataSizeUnit.MEGABYTES);
        suffixes.put("mebibyte", DataSizeUnit.MEBIBYTES);
        suffixes.put("megabytes", DataSizeUnit.MEGABYTES);
        suffixes.put("mebibytes", DataSizeUnit.MEBIBYTES);
        suffixes.put("G", DataSizeUnit.GIGABYTES);
        suffixes.put("GB", DataSizeUnit.GIGABYTES);
        suffixes.put("GiB", DataSizeUnit.GIBIBYTES);
        suffixes.put("gigabyte", DataSizeUnit.GIGABYTES);
        suffixes.put("gibibyte", DataSizeUnit.GIBIBYTES);
        suffixes.put("gigabytes", DataSizeUnit.GIGABYTES);
        suffixes.put("gibibytes", DataSizeUnit.GIBIBYTES);
        suffixes.put("T", DataSizeUnit.TERABYTES);
        suffixes.put("TB", DataSizeUnit.TERABYTES);
        suffixes.put("TiB", DataSizeUnit.TEBIBYTES);
        suffixes.put("terabyte", DataSizeUnit.TERABYTES);
        suffixes.put("tebibyte", DataSizeUnit.TEBIBYTES);
        suffixes.put("terabytes", DataSizeUnit.TERABYTES);
        suffixes.put("tebibytes", DataSizeUnit.TEBIBYTES);
        suffixes.put("P", DataSizeUnit.PETABYTES);
        suffixes.put("PB", DataSizeUnit.PETABYTES);
        suffixes.put("PiB", DataSizeUnit.PEBIBYTES);
        suffixes.put("petabyte", DataSizeUnit.PETABYTES);
        suffixes.put("pebibyte", DataSizeUnit.PEBIBYTES);
        suffixes.put("petabytes", DataSizeUnit.PETABYTES);
        suffixes.put("pebibytes", DataSizeUnit.PEBIBYTES);
        SUFFIXES = Collections.unmodifiableSortedMap(suffixes);
    }

    public static DataSize bytes(long count) {
        return new DataSize(count, DataSizeUnit.BYTES);
    }

    public static DataSize kilobytes(long count) {
        return new DataSize(count, DataSizeUnit.KILOBYTES);
    }

    public static DataSize megabytes(long count) {
        return new DataSize(count, DataSizeUnit.MEGABYTES);
    }

    public static DataSize gigabytes(long count) {
        return new DataSize(count, DataSizeUnit.GIGABYTES);
    }

    public static DataSize terabytes(long count) {
        return new DataSize(count, DataSizeUnit.TERABYTES);
    }

    public static DataSize petabytes(long count) {
        return new DataSize(count, DataSizeUnit.PETABYTES);
    }

    public static DataSize kibibytes(long count) {
        return new DataSize(count, DataSizeUnit.KIBIBYTES);
    }

    public static DataSize mebibytes(long count) {
        return new DataSize(count, DataSizeUnit.MEBIBYTES);
    }

    public static DataSize gibibytes(long count) {
        return new DataSize(count, DataSizeUnit.GIBIBYTES);
    }

    public static DataSize tebibytes(long count) {
        return new DataSize(count, DataSizeUnit.TEBIBYTES);
    }

    public static DataSize pebibytes(long count) {
        return new DataSize(count, DataSizeUnit.PEBIBYTES);
    }

    @JsonCreator
    public static DataSize parse(CharSequence size) {
        return parse(size, DataSizeUnit.BYTES);
    }

    public static DataSize parse(CharSequence size, DataSizeUnit defaultUnit) {
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        final long count = Long.parseLong(matcher.group(1));
        final String unit = matcher.group(2);
        final DataSizeUnit dataSizeUnit = Strings.isNullOrEmpty(unit) ? defaultUnit : SUFFIXES.get(unit);
        if (dataSizeUnit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new DataSize(count, dataSizeUnit);
    }

    private final long count;
    private final DataSizeUnit unit;

    private DataSize(long count, DataSizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    public long getQuantity() {
        return count;
    }

    public DataSizeUnit getUnit() {
        return unit;
    }

    public long toBytes() {
        return DataSizeUnit.BYTES.convert(count, unit);
    }

    public long toKilobytes() {
        return DataSizeUnit.KILOBYTES.convert(count, unit);
    }

    public long toMegabytes() {
        return DataSizeUnit.MEGABYTES.convert(count, unit);
    }

    public long toGigabytes() {
        return DataSizeUnit.GIGABYTES.convert(count, unit);
    }

    public long toTerabytes() {
        return DataSizeUnit.TERABYTES.convert(count, unit);
    }

    public long toPetabytes() {
        return DataSizeUnit.PETABYTES.convert(count, unit);
    }

    public long toKibibytes() {
        return DataSizeUnit.KIBIBYTES.convert(count, unit);
    }

    public long toMebibytes() {
        return DataSizeUnit.MEBIBYTES.convert(count, unit);
    }

    public long toGibibytes() {
        return DataSizeUnit.GIBIBYTES.convert(count, unit);
    }

    public long toTebibytes() {
        return DataSizeUnit.TEBIBYTES.convert(count, unit);
    }

    public long toPebibytes() {
        return DataSizeUnit.PEBIBYTES.convert(count, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }
        final DataSize size = (DataSize) obj;
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
        if (count == 1L) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    @Override
    public int compareTo(DataSize other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toBytes(), other.toBytes());
    }

    @SuppressWarnings("deprecation")
    public Size toSize() {
        switch (unit) {
            case BYTES:
                return Size.bytes(count);
            case KIBIBYTES:
                return Size.kilobytes(count);
            case MEBIBYTES:
                return Size.megabytes(count);
            case GIBIBYTES:
                return Size.gigabytes(count);
            case TEBIBYTES:
                return Size.terabytes(count);
            case PEBIBYTES:
                return Size.terabytes(count * 1024L);
            case KILOBYTES:
            case MEGABYTES:
            case GIGABYTES:
            case TERABYTES:
            case PETABYTES:
                return Size.bytes(toBytes());

            default:
                throw new IllegalArgumentException("Unknown unit: " + getUnit());
        }
    }

    @SuppressWarnings("deprecation")
    public static DataSize fromSize(Size size) {
        switch (size.getUnit()) {
            case BYTES:
                return DataSize.bytes(size.toBytes());
            case KILOBYTES:
                return DataSize.kibibytes(size.toKilobytes());
            case MEGABYTES:
                return DataSize.mebibytes(size.toMegabytes());
            case GIGABYTES:
                return DataSize.gibibytes(size.toGigabytes());
            case TERABYTES:
                return DataSize.tebibytes(size.toTerabytes());
            default:
                throw new IllegalArgumentException("Unknown unit: " + size.getUnit());
        }
    }
}