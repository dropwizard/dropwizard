package io.dropwizard.util;

/**
 * A unit of size. See https://en.wikipedia.org/wiki/Units_of_information.
 */
public enum SizeUnit {
    /**
     * Bytes.
     */
    BYTES(8),

    // OSI Decimal Size Units

    /**
     * Kilobytes.
     */
    KILOBYTES(8L * 1000),

    /**
     * Megabytes.
     */
    MEGABYTES(8L * 1000 * 1000),

    /**
     * Gigabytes.
     */
    GIGABYTES(8L * 1000 * 1000 * 1000),

    /**
     * Terabytes.
     */
    TERABYTES(8L * 1000 * 1000 * 1000 * 1000),

    /**
     * Petabytes.
     */
    PETABYTES(8L * 1000 * 1000 * 1000 * 1000 * 1000),

    // IEC Binary Size Units

    /**
     * Kibibytes.
     */
    KIBIBYTES(8L * 1024),

    /**
     * Mebibytes.
     */
    MEBIBYTES(8L * 1024 * 1024),

    /**
     * Gibibytes.
     */
    GIBIBYTES(8L * 1024 * 1024 * 1024),

    /**
     * Tebibytes.
     */
    TEBIBYTES(8L * 1024 * 1024 * 1024 * 1024),

    /**
     * Pebibytes.
     */
    PEBIBYTES(8L * 1024 * 1024 * 1024 * 1024 * 1024);

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
     * @return {@code l} of the current units in gigabytes
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

    /**
     * Converts the given number of the current units into petabytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in petabytes
     */
    public long toPetabytes(long l) {
        return PETABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into kibibytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in kibibytes
     */
    public long toKibibytes(long l) {
        return KIBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into mebibytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in mebibytes
     */
    public long toMebibytes(long l) {
        return MEBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into gibibytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in gibibytes
     */
    public long toGibibytes(long l) {
        return GIBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into tebibytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in tebibytes
     */
    public long toTebibytes(long l) {
        return TEBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into pebibytes.
     *
     * @param l    the magnitude of the size in the current unit
     * @return {@code l} of the current units in pebibytes
     */
    public long toPebibytes(long l) {
        return PEBIBYTES.convert(l, this);
    }
}
