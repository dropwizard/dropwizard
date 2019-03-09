package io.dropwizard.util;

/**
 * A unit of information using SI and IEC prefixes.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Units_of_information#Systematic_multiples">Units of information on Wikipedia</a>
 * @see <a href="https://en.wikipedia.org/wiki/Binary_prefix">Binary prefix on Wikipedia</a>
 */
public enum DataSizeUnit {
    /**
     * Bytes (8 bits).
     */
    BYTES(8L),

    // OSI Decimal Size Units

    /**
     * Kilobytes (1000 bytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Kilo-">Kilo-</a>
     */
    KILOBYTES(8L * 1000L),

    /**
     * Megabytes (1000 kilobytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Mega-">Mega-</a>
     */
    MEGABYTES(8L * 1000L * 1000L),

    /**
     * Gigabytes (1000 megabytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Giga-">Giga-</a>
     */
    GIGABYTES(8L * 1000L * 1000L * 1000L),

    /**
     * Terabytes (1000 gigabytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Tera-">Tera-</a>
     */
    TERABYTES(8L * 1000L * 1000L * 1000L * 1000L),

    /**
     * Petabytes (1000 terabytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Peta-">Peta-</a>
     */
    PETABYTES(8L * 1000L * 1000L * 1000L * 1000L * 1000L),

    // IEC Binary Size Units
    /**
     * Kibibytes (1024 bytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Kibi-">Kibi-</a>
     */
    KIBIBYTES(8L * 1024L),

    /**
     * Mebibytes (1024 kibibytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Mebi-">Mebi-</a>
     */
    MEBIBYTES(8L * 1024L * 1024L),

    /**
     * Gibibytes (1024 mebibytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Gibi-">Gibi-</a>
     */
    GIBIBYTES(8L * 1024L * 1024L * 1024L),

    /**
     * Tebibytes (1024 gibibytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Tebi-">Tebi-</a>
     */
    TEBIBYTES(8L * 1024L * 1024L * 1024L * 1024L),

    /**
     * Pebibytes (1024 tebibytes).
     *
     * @see <a href="https://en.wikipedia.org/wiki/Pebi-">Pebi-</a>
     */
    PEBIBYTES(8L * 1024L * 1024L * 1024L * 1024L * 1024L);

    private final long bits;

    DataSizeUnit(long bits) {
        this.bits = bits;
    }

    /**
     * Converts a size of the given unit into the current unit.
     *
     * @param size the magnitude of the size
     * @param unit the unit of the size
     * @return the given size in the current unit.
     */
    public long convert(long size, DataSizeUnit unit) {
        return (size * unit.bits) / bits;
    }

    /**
     * Converts the given number of the current units into bytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in bytes
     */
    public long toBytes(long l) {
        return BYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into kilobytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in kilobytes
     */
    public long toKilobytes(long l) {
        return KILOBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into megabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in megabytes
     */
    public long toMegabytes(long l) {
        return MEGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into gigabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in gigabytes
     */
    public long toGigabytes(long l) {
        return GIGABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into terabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in terabytes
     */
    public long toTerabytes(long l) {
        return TERABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into petabytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in petabytes
     */
    public long toPetabytes(long l) {
        return PETABYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into kibibytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in kibibytes
     */
    public long toKibibytes(long l) {
        return KIBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into mebibytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in mebibytes
     */
    public long toMebibytes(long l) {
        return MEBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into gibibytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in gibibytes
     */
    public long toGibibytes(long l) {
        return GIBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into tebibytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in tebibytes
     */
    public long toTebibytes(long l) {
        return TEBIBYTES.convert(l, this);
    }

    /**
     * Converts the given number of the current units into pebibytes.
     *
     * @param l the magnitude of the size in the current unit
     * @return {@code l} of the current units in pebibytes
     */
    public long toPebibytes(long l) {
        return PEBIBYTES.convert(l, this);
    }
}