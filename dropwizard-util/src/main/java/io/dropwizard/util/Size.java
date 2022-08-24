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
 * @deprecated Use {@link DataSize} for correct SI and IEC prefixes.
 */
@Deprecated
public class Size implements Comparable<Size>, Serializable {
    private static final long serialVersionUID = 6790991929249604526L;

    private static final Pattern SIZE_PATTERN = Pattern.compile("(\\d+)\\s*(\\S+)");
    private static final SortedMap<String, SizeUnit> SUFFIXES;

    static {
        final SortedMap<String, SizeUnit> suffixes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        suffixes.put("B", SizeUnit.BYTES);
        suffixes.put("byte", SizeUnit.BYTES);
        suffixes.put("bytes", SizeUnit.BYTES);
        suffixes.put("K", SizeUnit.KILOBYTES);
        suffixes.put("KB", SizeUnit.KILOBYTES);
        suffixes.put("KiB", SizeUnit.KILOBYTES);
        suffixes.put("kilobyte", SizeUnit.KILOBYTES);
        suffixes.put("kilobytes", SizeUnit.KILOBYTES);
        suffixes.put("M", SizeUnit.MEGABYTES);
        suffixes.put("MB", SizeUnit.MEGABYTES);
        suffixes.put("MiB", SizeUnit.MEGABYTES);
        suffixes.put("megabyte", SizeUnit.MEGABYTES);
        suffixes.put("megabytes", SizeUnit.MEGABYTES);
        suffixes.put("G", SizeUnit.GIGABYTES);
        suffixes.put("GB", SizeUnit.GIGABYTES);
        suffixes.put("GiB", SizeUnit.GIGABYTES);
        suffixes.put("gigabyte", SizeUnit.GIGABYTES);
        suffixes.put("gigabytes", SizeUnit.GIGABYTES);
        suffixes.put("T", SizeUnit.TERABYTES);
        suffixes.put("TB", SizeUnit.TERABYTES);
        suffixes.put("TiB", SizeUnit.TERABYTES);
        suffixes.put("terabyte", SizeUnit.TERABYTES);
        suffixes.put("terabytes", SizeUnit.TERABYTES);
        SUFFIXES = Collections.unmodifiableSortedMap(suffixes);
    }

    /**
     * Constructs a new {@link Size} object representing the specified amount of bytes.
     *
     * @param count the amount of bytes
     * @return the newly created {@link Size} object
     */
    public static Size bytes(long count) {
        return new Size(count, SizeUnit.BYTES);
    }

    /**
     * Constructs a new {@link Size} object representing the specified amount of kilobytes.
     *
     * @param count the amount of kilobytes
     * @return the newly created {@link Size} object
     */
    public static Size kilobytes(long count) {
        return new Size(count, SizeUnit.KILOBYTES);
    }

    /**
     * Constructs a new {@link Size} object representing the specified amount of megabytes.
     *
     * @param count the amount of megabytes
     * @return the newly created {@link Size} object
     */
    public static Size megabytes(long count) {
        return new Size(count, SizeUnit.MEGABYTES);
    }

    /**
     * Constructs a new {@link Size} object representing the specified amount of gigabytes.
     *
     * @param count the amount of gigabytes
     * @return the newly created {@link Size} object
     */
    public static Size gigabytes(long count) {
        return new Size(count, SizeUnit.GIGABYTES);
    }

    /**
     * Constructs a new {@link Size} object representing the specified amount of terabytes.
     *
     * @param count the amount of terabytes
     * @return the newly created {@link Size} object
     */
    public static Size terabytes(long count) {
        return new Size(count, SizeUnit.TERABYTES);
    }

    /**
     * Constructs a {@link Size} object from the given string representation.
     *
     * @param size the string representation to parse
     * @return the newly created {@link Size} object
     * @throws IllegalArgumentException if the input string cannot be parsed correctly
     */
    @JsonCreator
    public static Size parse(String size) {
        final Matcher matcher = SIZE_PATTERN.matcher(size);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid size: " + size);
        }

        final long count = Long.parseLong(matcher.group(1));
        final SizeUnit unit = SUFFIXES.get(matcher.group(2));
        if (unit == null) {
            throw new IllegalArgumentException("Invalid size: " + size + ". Wrong size unit");
        }

        return new Size(count, unit);
    }

    /**
     * The quantity of the current size
     */
    private final long count;

    /**
     * The unit of the current size
     */
    private final SizeUnit unit;

    private Size(long count, SizeUnit unit) {
        this.count = count;
        this.unit = requireNonNull(unit);
    }

    /**
     * Gets the quantity of the current {@link Size} object.
     *
     * @return the quantity of the current size
     */
    public long getQuantity() {
        return count;
    }

    /**
     * Returns the {@link SizeUnit size unit} of the current {@link Size} object.
     *
     * @return the unit of the current size
     */
    public SizeUnit getUnit() {
        return unit;
    }

    /**
     * Returns the quantity of the current {@link Size} object in bytes.
     *
     * @return the converted quantity
     */
    public long toBytes() {
        return SizeUnit.BYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Size} object in kilobytes.
     *
     * @return the converted quantity
     */
    public long toKilobytes() {
        return SizeUnit.KILOBYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Size} object in megabytes.
     *
     * @return the converted quantity
     */
    public long toMegabytes() {
        return SizeUnit.MEGABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Size} object in gigabytes.
     *
     * @return the converted quantity
     */
    public long toGigabytes() {
        return SizeUnit.GIGABYTES.convert(count, unit);
    }

    /**
     * Returns the quantity of the current {@link Size} object in terabytes.
     *
     * @return the converted quantity
     */
    public long toTerabytes() {
        return SizeUnit.TERABYTES.convert(count, unit);
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
        final Size size = (Size) obj;
        return this.compareTo(size) == 0;
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
    public int compareTo(Size other) {
        if (unit == other.unit) {
            return Long.compare(count, other.count);
        }

        return Long.compare(toBytes(), other.toBytes());
    }

    /**
     * Converts the current {@link Size} to a representation of the newer {@link DataSize} class.
     *
     * @return the converted {@link DataSize} instance
     * @since 2.0
     */
    public DataSize toDataSize() {
        switch (unit) {
            case BYTES:
                return DataSize.bytes(count);
            case KILOBYTES:
                return DataSize.kibibytes(count);
            case MEGABYTES:
                return DataSize.mebibytes(count);
            case GIGABYTES:
                return DataSize.gibibytes(count);
            case TERABYTES:
                return DataSize.tebibytes(count);
            default:
                throw new IllegalArgumentException("Unknown unit: " + getUnit());
        }
    }

    /**
     * Constructs a {@link Size} object from a given {@link DataSize} instance.
     *
     * @param dataSize the data size object to convert
     * @return the converted {@link Size} object
     * @since 2.0
     */
    public static Size fromDataSize(DataSize dataSize) {
        switch (dataSize.getUnit()) {
            case BYTES:
                return Size.bytes(dataSize.getQuantity());
            case KIBIBYTES:
                return Size.kilobytes(dataSize.getQuantity());
            case KILOBYTES:
                return Size.bytes(dataSize.toBytes());
            case MEBIBYTES:
                return Size.megabytes(dataSize.getQuantity());
            case MEGABYTES:
                return Size.bytes(dataSize.toBytes());
            case GIBIBYTES:
                return Size.gigabytes(dataSize.getQuantity());
            case GIGABYTES:
                return Size.bytes(dataSize.toBytes());
            case TEBIBYTES:
                return Size.terabytes(dataSize.getQuantity());
            case TERABYTES:
                return Size.bytes(dataSize.toBytes());
            case PEBIBYTES:
                return Size.terabytes(dataSize.toTebibytes() * 1024L);
            case PETABYTES:
                return Size.bytes(dataSize.toBytes());
            default:
                throw new IllegalArgumentException("Unknown unit: " + dataSize.getUnit());
        }
    }
}
