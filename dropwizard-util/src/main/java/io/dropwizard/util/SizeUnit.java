package io.dropwizard.util;

/**
 * A unit of size.
 */
public enum SizeUnit {
    /**
     * Bytes.
     */
    BYTES(8),

    /**
     * Kilobytes.
     */
    KILOBYTES(8L * 1024),

    /**
     * Megabytes.
     */
    MEGABYTES(8L * 1024 * 1024),

    /**
     * Gigabytes.
     */
    GIGABYTES(8L * 1024 * 1024 * 1024),

    /**
     * Terabytes.
     */
    TERABYTES(8L * 1024 * 1024 * 1024 * 1024);

    private final long bits;

    SizeUnit(long bits) {
        this.bits = bits;
    }

    /**
     * Converts a size of the given unit into the current unit.
     *
     * @param size    the magnitude of the size
     * @param unit    the unit of the size
     * @return the given size in the current unit.
     */
    public long convert(long size, SizeUnit unit) {
        return (size * unit.bits) / bits;
    }

    /**
     * Converts the given number of the current units into bytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in bytes
     */
    public long toBytes(long l) {
        return BYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into kilobytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in kilobytes
     */
    public long toKilobytes(long l) {
        return KILOBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into megabytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in megabytes
     */
    public long toMegabytes(long l) {
        return MEGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into gigabytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in bytes
     */
    public long toGigabytes(long l) {
        return GIGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into terabytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in terabytes
     */
    public long toTerabytes(long l) {
        return TERABYTES.convert(l, this);
    }
}
