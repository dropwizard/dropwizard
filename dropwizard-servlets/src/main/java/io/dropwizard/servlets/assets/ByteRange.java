package io.dropwizard.servlets.assets;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;

@Immutable
public final class ByteRange {

    private final int start;
    private final int end;

    public ByteRange(final int start, final int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public static ByteRange parse(final String byteRange,
                                  final int resourceLength) {
        // missing separator
        if (byteRange.indexOf("-") == -1) {
            final int start = Integer.parseInt(byteRange);
            return new ByteRange(start, resourceLength - 1);
        }
        // negative range
        if (byteRange.indexOf("-") == 0) {
            final int start = Integer.parseInt(byteRange);
            return new ByteRange(resourceLength + start, resourceLength - 1);
        }
        final String[] parts = byteRange.split("-");
        if (parts.length == 2) {
            final int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            if (end > resourceLength) {
                end = resourceLength - 1;
            }
            return new ByteRange(start, end);
        } else {
            final int start = Integer.parseInt(parts[0]);
            return new ByteRange(start, resourceLength - 1);
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        final ByteRange other = (ByteRange) obj;
        return Objects.equals(start, other.start) && Objects.equals(end, other.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return String.format("%d-%d", start, end);
    }
}
