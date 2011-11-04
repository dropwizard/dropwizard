package com.yammer.dropwizard.util;

public enum SizeUnit {
    BYTES(8),
    KILOBYTES(8L * 1024),
    MEGABYTES(8L * 1024 * 1024),
    GIGABYTES(8L * 1024 * 1024 * 1024),
    TERABYTES(8L * 1024 * 1024 * 1024 * 1024);

    private final long bits;

    private SizeUnit(long bits) {
        this.bits = bits;
    }

    public long convert(long size, SizeUnit unit) {
        return (size * unit.bits) / bits;
    }

    public long toBytes(long d) {
        return BYTES.convert(d, this);
    }

    public long toKilobytes(long d) {
        return KILOBYTES.convert(d, this);
    }

    public long toMegabytes(long d) {
        return MEGABYTES.convert(d, this);
    }

    public long toGigabytes(long d) {
        return GIGABYTES.convert(d, this);
    }

    public long toTerabytes(long d) {
        return TERABYTES.convert(d, this);
    }
}
