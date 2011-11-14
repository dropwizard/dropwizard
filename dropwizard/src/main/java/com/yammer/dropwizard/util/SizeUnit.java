package com.yammer.dropwizard.util;

public enum SizeUnit {
    BYTES(8),
    KILOBYTES(8L * 1024),
    MEGABYTES(8L * 1024 * 1024),
    GIGABYTES(8L * 1024 * 1024 * 1024),
    TERABYTES(8L * 1024 * 1024 * 1024 * 1024);

    private final long bits;

    SizeUnit(long bits) {
        this.bits = bits;
    }

    public long convert(long size, SizeUnit unit) {
        return (size * unit.bits) / bits;
    }

    public long toBytes(long l) {
        return BYTES.convert(l, this);
    }

    public long toKilobytes(long l) {
        return KILOBYTES.convert(l, this);
    }

    public long toMegabytes(long l) {
        return MEGABYTES.convert(l, this);
    }

    public long toGigabytes(long l) {
        return GIGABYTES.convert(l, this);
    }

    public long toTerabytes(long l) {
        return TERABYTES.convert(l, this);
    }
}
