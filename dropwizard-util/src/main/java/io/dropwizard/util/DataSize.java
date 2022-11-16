package io.dropwizard.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;
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
public class DataSize implements Comparable<DataSize>, Serializable {
    private static final long serialVersionUID = 8517642678733072800L;

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

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of bytes.
     *
     * @param count the amount of bytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize bytes(long count) {
        return new DataSize(count, DataSizeUnit.BYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of kilobytes.
     *
     * @param count the amount of kilobytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize kilobytes(long count) {
        return new DataSize(count, DataSizeUnit.KILOBYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of megabytes.
     *
     * @param count the amount of megabytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize megabytes(long count) {
        return new DataSize(count, DataSizeUnit.MEGABYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of gigabytes.
     *
     * @param count the amount of gigabytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize gigabytes(long count) {
        return new DataSize(count, DataSizeUnit.GIGABYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of terabytes.
     *
     * @param count the amount of terabytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize terabytes(long count) {
        return new DataSize(count, DataSizeUnit.TERABYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of petabytes.
     *
     * @param count the amount of petabytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize petabytes(long count) {
        return new DataSize(count, DataSizeUnit.PETABYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of kibibytes.
     *
     * @param count the amount of kibibytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize kibibytes(long count) {
        return new DataSize(count, DataSizeUnit.KIBIBYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of mebibytes.
     *
     * @param count the amount of mebibytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize mebibytes(long count) {
        return new DataSize(count, DataSizeUnit.MEBIBYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of gibibytes.
     *
     * @param count the amount of gibibytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize gibibytes(long count) {
        return new DataSize(count, DataSizeUnit.GIBIBYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of tebibytes.
     *
     * @param count the amount of tebibytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize tebibytes(long count) {
        return new DataSize(count, DataSizeUnit.TEBIBYTES);
    }

    /**
     * Constructs a new {@link DataSize} object representing the specified amount of pebibytes.
     *
     * @param count the amount of pebibytes
     * @return the newly created {@link DataSize} object
     */
    public static DataSize pebibytes(long count) {
        return new DataSize(count, DataSizeUnit.PEBIBYTES);
    }

    /**
     * Parses a given {@link CharSequence} to a {@link DataSize} object.
     * If no unit is provided by the input sequence, a default unit of {@link DataSizeUnit#BYTES} is used.
     *
     * @param size the string representation of the {@link DataSize} to parse
     * @return a valid new {@link DataSize} object representing the parsed string
     */
    @JsonCreator
    public static DataSize parse(CharSequence size) {
        return parse(size, DataSizeUnit.BYTES);
    }

    /**
     * Parses a given {@link CharSequence} to a {@link DataSize} object.
     * If no unit is provided by the input sequence, the default unit parameter is used.
     *
     * @param size the string representation of the {@link DataSize} to parse
     * @param defaultUnit the fallback default unit to use for the newly created {@link DataSize}
     * @return a valid new {@link DataSize} object representing the parsed string
     * @throws IllegalArgumentException if the input sequence cannot be parsed correctly
     */
    public static DataSize parse(CharSequence size, DataSizeUnit defaultUnit) {
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        final long count = Long.parseLong(matcher.group(1));
        final String unit = matcher.group(2);
        final DataSizeUnit dataSizeUnit = unit == null || unit.isEmpty() ? defaultUnit : SUFFIXES.get(unit);
        if (dataSizeUnit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new DataSize(count, dataSizeUnit);
    }

    /**
     * The quantity of the current data size
     */
    private final long count;

    /**
     * The unit of the current data size
     */
    private final DataSizeUnit unit;

    private DataSize(long count, DataSizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    /**
     * Gets the quantity of the current {@link DataSize} object.
     *
     * @return the quantity of the current data size
     */
    public long getQuantity() {
        return count;
    }

    /**
     * Returns the {@link DataSizeUnit data size unit} of the current {@link DataSize} object.
     *
     * @return the unit of the current data size
     */
    public DataSizeUnit getUnit() {
        return unit;
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in bytes.
     *
     * @return the converted quantity
     */
    public long toBytes() {
        return DataSizeUnit.BYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in kilobytes.
     *
     * @return the converted quantity
     */
    public long toKilobytes() {
        return DataSizeUnit.KILOBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in megabytes.
     *
     * @return the converted quantity
     */
    public long toMegabytes() {
        return DataSizeUnit.MEGABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in gigabytes.
     *
     * @return the converted quantity
     */
    public long toGigabytes() {
        return DataSizeUnit.GIGABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in terabytes.
     *
     * @return the converted quantity
     */
    public long toTerabytes() {
        return DataSizeUnit.TERABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in petabytes.
     *
     * @return the converted quantity
     */
    public long toPetabytes() {
        return DataSizeUnit.PETABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in kibibytes.
     *
     * @return the converted quantity
     */
    public long toKibibytes() {
        return DataSizeUnit.KIBIBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in mebibytes.
     *
     * @return the converted quantity
     */
    public long toMebibytes() {
        return DataSizeUnit.MEBIBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in gibibytes.
     *
     * @return the converted quantity
     */
    public long toGibibytes() {
        return DataSizeUnit.GIBIBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in gebibytes.
     *
     * @return the converted quantity
     */
    public long toTebibytes() {
        return DataSizeUnit.TEBIBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link DataSize} object in pebibytes.
     *
     * @return the converted quantity
     */
    public long toPebibytes() {
        return DataSizeUnit.PEBIBYTES.convert(count, unit);
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
        final DataSize size = (DataSize) obj;
        return (count == size.count) && (unit == size.unit);
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
        if (count == 1L) {
            units = units.substring(0, units.length() - 1);
        }
        return Long.toString(count) + ' ' + units;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(DataSize other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toBytes(), other.toBytes());
    }
}
